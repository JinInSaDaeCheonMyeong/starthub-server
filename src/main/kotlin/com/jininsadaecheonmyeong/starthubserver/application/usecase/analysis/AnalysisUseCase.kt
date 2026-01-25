package com.jininsadaecheonmyeong.starthubserver.application.usecase.analysis

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.domain.entity.analysis.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.exception.analysis.BmcAccessDeniedException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.analysis.CompetitorAnalysisNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.bmc.BmcNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.analysis.CompetitorAnalysisRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.PerplexitySearchService
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchRequest
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.analysis.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.CompetitorInfo
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.CompetitorScale
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.GlobalExpansionStrategy
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.StrengthsAnalysis
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.UserBmcSummary
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.UserScaleAnalysis
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.WeaknessesAnalysis
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
@Transactional(readOnly = true)
class AnalysisUseCase(
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val competitorAnalysisRepository: CompetitorAnalysisRepository,
    private val perplexitySearchService: PerplexitySearchService,
    @Qualifier("openAiChatModel") private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(AnalysisUseCase::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val ongoingTasks = ConcurrentHashMap<Long, Deferred<CompetitorAnalysisResponse>>()

    companion object {
        private const val MAX_COMPETITORS = 4
        private const val MIN_KEYWORD_LENGTH = 4
        private const val MAX_KEYWORDS = 5
        private const val MIN_SENTENCE_LENGTH = 3
        private const val SCALE_TEXT_MAX_LENGTH = 200

        private const val FALLBACK_SCALE = "중간 규모"
        private const val FALLBACK_MARKET_SHARE = "5-10%"

        private val SENTENCE_TERMINATORS = listOf("습니다", "합니다", "됩니다", "입니다", "있습니다", "없습니다", "갑니다", "옵니다")
        private val SPECIAL_CHARS_TO_CLEAN = listOf("**", "*", "##")
    }

    @Transactional
    suspend fun analyzeCompetitors(request: CompetitorAnalysisRequest): CompetitorAnalysisResponse {
        val user = userAuthenticationHolder.current()
        val userBmc =
            withContext(Dispatchers.IO) {
                businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
            }
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        validateUserAccess(userBmc, user)

        val existingAnalysis =
            withContext(Dispatchers.IO) {
                competitorAnalysisRepository.findByBusinessModelCanvasAndDeletedFalse(userBmc)
            }
        if (existingAnalysis.isPresent) {
            return deserializeAnalysisResponse(existingAnalysis.get())
        }

        val ongoingTask = getOngoingTask(request.bmcId)
        if (ongoingTask != null) {
            try {
                return ongoingTask.await()
            } catch (e: Exception) {
                logger.error("진행 중인 분석 대기 실패 - BMC ID: {}", request.bmcId, e)
            }
        }

        return performAnalysisSuspend(user, userBmc)
    }

    @Transactional
    suspend fun regenerateAnalysis(bmcId: Long): CompetitorAnalysisResponse {
        val user = userAuthenticationHolder.current()
        val userBmc =
            withContext(Dispatchers.IO) {
                businessModelCanvasRepository.findByIdAndDeletedFalse(bmcId)
            }
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }
        validateUserAccess(userBmc, user)
        return performAnalysisSuspend(user, userBmc)
    }

    @Transactional
    suspend fun performAnalysisInternal(
        user: User,
        userBmc: BusinessModelCanvas,
    ): CompetitorAnalysisResponse {
        val existingAnalysis =
            withContext(Dispatchers.IO) {
                competitorAnalysisRepository.findByBusinessModelCanvasAndDeletedFalse(userBmc)
            }
        if (existingAnalysis.isPresent) {
            return deserializeAnalysisResponse(existingAnalysis.get())
        }

        return performAnalysisSuspend(user, userBmc)
    }

    fun performAnalysisAsync(
        user: User,
        businessModelCanvas: BusinessModelCanvas,
    ) {
        val bmcId = businessModelCanvas.id!!

        coroutineScope.launch {
            try {
                getOrCreateTask(bmcId) {
                    performAnalysisInternal(user, businessModelCanvas)
                }
            } catch (e: Exception) {
                logger.error("경쟁사 분석 실패 - BMC ID: {}", bmcId, e)
            }
        }
    }

    private suspend fun performAnalysisSuspend(
        user: User,
        userBmc: BusinessModelCanvas,
    ): CompetitorAnalysisResponse {
        val searchKeywords = generateSearchKeywords(userBmc)

        val competitors =
            try {
                val searchRequest = createSearchRequest(userBmc, searchKeywords)
                val searchResults = perplexitySearchService.searchCompetitors(searchRequest)

                searchResults.map { result ->
                    CompetitorInfo(
                        name = result.title,
                        description = result.snippet,
                        logoUrl = result.thumbnailUrl,
                        websiteUrl = result.url,
                    )
                }
            } catch (e: Exception) {
                logger.error("경쟁사 검색 실패: {}", e.message)
                emptyList()
            }

        val analysisResponse =
            try {
                val analysisPrompt = buildAnalysisPrompt(userBmc, competitors)
                val gptResponse = chatModel.call(analysisPrompt)
                parseGptResponse(gptResponse, userBmc, competitors)
            } catch (e: Exception) {
                logger.error("경쟁사 분석 실패: {}", e.message)
                createFallbackResponse(userBmc, competitors)
            }
        saveAnalysis(user, userBmc, analysisResponse)
        return analysisResponse
    }

    private fun saveAnalysis(
        user: User,
        bmc: BusinessModelCanvas,
        response: CompetitorAnalysisResponse,
    ) {
        val existingAnalysis = competitorAnalysisRepository.findByBusinessModelCanvasAndDeletedFalse(bmc)

        if (existingAnalysis.isPresent) {
            val analysis = existingAnalysis.get()
            analysis.userBmcSummary = objectMapper.writeValueAsString(response.userBmc)
            analysis.userScaleAnalysis = objectMapper.writeValueAsString(response.userScale)
            analysis.strengthsAnalysis = objectMapper.writeValueAsString(response.strengths)
            analysis.weaknessesAnalysis = objectMapper.writeValueAsString(response.weaknesses)
            analysis.globalExpansionStrategy = objectMapper.writeValueAsString(response.globalExpansionStrategy)
            competitorAnalysisRepository.save(analysis)
        } else {
            val newAnalysis =
                CompetitorAnalysis(
                    user = user,
                    businessModelCanvas = bmc,
                    userBmcSummary = objectMapper.writeValueAsString(response.userBmc),
                    userScaleAnalysis = objectMapper.writeValueAsString(response.userScale),
                    strengthsAnalysis = objectMapper.writeValueAsString(response.strengths),
                    weaknessesAnalysis = objectMapper.writeValueAsString(response.weaknesses),
                    globalExpansionStrategy = objectMapper.writeValueAsString(response.globalExpansionStrategy),
                )
            competitorAnalysisRepository.save(newAnalysis)
        }
    }

    fun getAnalysisByBmcId(bmcId: Long): CompetitorAnalysisResponse {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }
        validateUserAccess(bmc, user)

        val analysis =
            competitorAnalysisRepository.findByBusinessModelCanvasAndDeletedFalse(bmc)
                .orElseThrow { CompetitorAnalysisNotFoundException("경쟁사 분석을 찾을 수 없습니다.") }

        return deserializeAnalysisResponse(analysis)
    }

    fun getAllAnalysesByUser(): List<CompetitorAnalysisResponse> {
        val user = userAuthenticationHolder.current()
        val analyses = competitorAnalysisRepository.findAllByUserAndDeletedFalse(user)

        return analyses.map { analysis ->
            deserializeAnalysisResponse(analysis)
        }
    }

    private fun deserializeAnalysisResponse(analysis: CompetitorAnalysis): CompetitorAnalysisResponse {
        return CompetitorAnalysisResponse(
            bmcId = analysis.businessModelCanvas.id!!,
            userBmc = objectMapper.readValue(analysis.userBmcSummary, UserBmcSummary::class.java),
            userScale = objectMapper.readValue(analysis.userScaleAnalysis, UserScaleAnalysis::class.java),
            strengths = objectMapper.readValue(analysis.strengthsAnalysis, StrengthsAnalysis::class.java),
            weaknesses = objectMapper.readValue(analysis.weaknessesAnalysis, WeaknessesAnalysis::class.java),
            globalExpansionStrategy = objectMapper.readValue(analysis.globalExpansionStrategy, GlobalExpansionStrategy::class.java),
            createdAt = analysis.createdAt,
        )
    }

    private fun createSearchRequest(
        userBmc: BusinessModelCanvas,
        searchKeywords: List<String>,
    ): SearchRequest {
        return SearchRequest(
            query = searchKeywords.joinToString(" ") + " 회사 서비스",
            maxResults = MAX_COMPETITORS,
            bmcContext =
                com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.BmcContext(
                    title = userBmc.title,
                    valueProposition = userBmc.valueProposition,
                    customerSegments = userBmc.customerSegments,
                    channels = userBmc.channels,
                    revenueStreams = userBmc.revenueStreams,
                ),
        )
    }

    private fun validateUserAccess(
        userBmc: BusinessModelCanvas,
        user: User,
    ) {
        if (!userBmc.isOwner(user)) {
            throw BmcAccessDeniedException("해당 BMC에 접근할 권한이 없습니다.")
        }
    }

    private fun generateSearchKeywords(userBmc: BusinessModelCanvas): List<String> {
        val allText = listOfNotNull(userBmc.title, userBmc.valueProposition, userBmc.customerSegments).joinToString(" ")

        val keywords =
            allText
                .replace("\n", " ")
                .replace("-", " ")
                .split(Regex("[,.;]"))
                .map { it.trim() }
                .filter { keyword ->
                    keyword.isNotBlank() &&
                        keyword.length >= MIN_KEYWORD_LENGTH &&
                        !keyword.startsWith("(") &&
                        !keyword.startsWith("[")
                }
                .distinct()
                .take(MAX_KEYWORDS)

        return keywords.ifEmpty { listOf(userBmc.title) }
    }

    private fun parseGptResponse(
        gptResponse: String,
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
    ): CompetitorAnalysisResponse {
        return CompetitorAnalysisResponse(
            bmcId = userBmc.id!!,
            userBmc = createUserBmcSummary(userBmc, gptResponse),
            userScale = parseUserScaleAnalysis(gptResponse, competitors),
            strengths = parseStrengthsAnalysis(gptResponse),
            weaknesses = parseWeaknessesAnalysis(gptResponse),
            globalExpansionStrategy = parseGlobalExpansionStrategy(gptResponse),
            createdAt = LocalDateTime.now(),
        )
    }

    private fun createUserBmcSummary(
        userBmc: BusinessModelCanvas,
        gptResponse: String,
    ): UserBmcSummary {
        val bmcSection = extractSection(gptResponse, "0. BMC 핵심 강점", "1. 사용자 규모 분석")
        val keyStrengths = extractListValues(bmcSection, "핵심 강점", 3)

        return UserBmcSummary(
            title = userBmc.title,
            valueProposition = userBmc.valueProposition,
            targetCustomer = userBmc.customerSegments,
            keyStrengths =
                keyStrengths.ifEmpty {
                    extractKeyStrengthsFallback(userBmc)
                },
        )
    }

    private fun extractKeyStrengthsFallback(userBmc: BusinessModelCanvas): List<String> {
        return buildList {
            if (!userBmc.valueProposition.isNullOrBlank()) add("명확한 가치 제안")
            if (!userBmc.keyResources.isNullOrBlank()) add("핵심 자원 보유")
            if (!userBmc.revenueStreams.isNullOrBlank()) add("수익 모델 확립")
        }.ifEmpty { listOf("비즈니스 모델 수립 완료") }
    }

    private fun parseUserScaleAnalysis(
        gptResponse: String,
        competitors: List<CompetitorInfo>,
    ): UserScaleAnalysis {
        val scaleSection = extractSection(gptResponse, "1. 사용자 규모 분석", "2. 강점 분석")

        return UserScaleAnalysis(
            estimatedUserBase = extractValue(scaleSection, "예상_사용자_기반_규모", "소규모 스타트업 단계"),
            marketPosition = extractValue(scaleSection, "시장_내_위치", "신규 진입자"),
            growthPotential = extractValue(scaleSection, "성장_잠재력", "높은 성장 가능성"),
            competitorComparison = createCompetitorScales(scaleSection, competitors),
        )
    }

    private fun createCompetitorScales(
        scaleSection: String,
        competitors: List<CompetitorInfo>,
    ): List<CompetitorScale> {
        if (competitors.isEmpty()) return emptyList()

        val competitorAnalysisSection = extractCompetitorAnalysisSection(scaleSection)

        return competitors.take(MAX_COMPETITORS).map { competitor ->
            val (scale, share, similarities, differences) = extractCompetitorDetailedInfo(competitorAnalysisSection, competitor.name)
            CompetitorScale(
                name = competitor.name,
                logoUrl = competitor.logoUrl,
                websiteUrl = competitor.websiteUrl,
                estimatedScale = scale,
                marketShare = share,
                similarities = similarities,
                differences = differences,
            )
        }
    }

    private fun extractCompetitorAnalysisSection(scaleSection: String): String {
        val startMarker = "[경쟁사별_분석]"
        val endMarker = "[경쟁사별_분석_끝]"

        val startIndex = scaleSection.indexOf(startMarker, ignoreCase = true)
        if (startIndex == -1) {
            val fallbackIndex = scaleSection.indexOf("경쟁사별 분석", ignoreCase = true)
            return if (fallbackIndex != -1) scaleSection.substring(fallbackIndex) else scaleSection
        }

        val endIndex = scaleSection.indexOf(endMarker, startIndex, ignoreCase = true)
        return if (endIndex != -1) {
            scaleSection.substring(startIndex, endIndex + endMarker.length)
        } else {
            scaleSection.substring(startIndex)
        }
    }

    private fun extractCompetitorDetailedInfo(
        section: String,
        competitorName: String,
    ): CompetitorDetailedInfo {
        val cleanCompetitorName = cleanSpecialCharacters(competitorName)

        val startMarker = "[경쟁사:$cleanCompetitorName]"
        val endMarker = "[경쟁사_끝:$cleanCompetitorName]"

        val startIndex = section.indexOf(startMarker, ignoreCase = true)
        if (startIndex != -1) {
            val endIndex = section.indexOf(endMarker, startIndex, ignoreCase = true)
            val competitorBlock =
                if (endIndex != -1) {
                    section.substring(startIndex + startMarker.length, endIndex)
                } else {
                    section.substring(startIndex + startMarker.length)
                }

            if (competitorBlock.isNotBlank()) {
                val scale = extractBracketField(competitorBlock, "예상_규모", FALLBACK_SCALE)
                val share = extractBracketField(competitorBlock, "시장_점유율", FALLBACK_MARKET_SHARE)
                val similarities = extractSentenceList(competitorBlock, "유사점", MAX_COMPETITORS)
                val differences = extractSentenceList(competitorBlock, "차이점", MAX_COMPETITORS)

                return CompetitorDetailedInfo(scale, share, similarities, differences)
            }
        }

        val escapedName = Regex.escape(cleanCompetitorName)
        val competitorBlockPattern =
            Regex(
                "-\\s*$escapedName[:\\s]*(.+?)(?=-\\s*[\\w가-힣]+:|$)",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
            )
        val competitorBlock = competitorBlockPattern.find(section)?.groupValues?.get(1)?.trim()

        if (competitorBlock.isNullOrBlank()) {
            return CompetitorDetailedInfo(FALLBACK_SCALE, FALLBACK_MARKET_SHARE, emptyList(), emptyList())
        }

        val scale = extractFallbackField(competitorBlock, "예상\\s*규모", FALLBACK_SCALE)
        val share = extractFallbackField(competitorBlock, "시장\\s*점유율", FALLBACK_MARKET_SHARE)
        val similarities = extractFallbackSentenceList(competitorBlock, "유사점", MAX_COMPETITORS)
        val differences = extractFallbackSentenceList(competitorBlock, "차이점", MAX_COMPETITORS)

        return CompetitorDetailedInfo(scale, share, similarities, differences)
    }

    private fun cleanSpecialCharacters(text: String): String {
        var result = text
        SPECIAL_CHARS_TO_CLEAN.forEach { char ->
            result = result.replace(char, "")
        }
        return result.trim()
    }

    private fun extractBracketField(
        block: String,
        fieldName: String,
        fallback: String,
    ): String {
        val pattern = Regex("\\[$fieldName]\\s*(.+?)(?=\\[|$)", RegexOption.DOT_MATCHES_ALL)
        return pattern.find(block)?.groupValues?.get(1)
            ?.replace("\n", " ")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.take(SCALE_TEXT_MAX_LENGTH)
            ?: fallback
    }

    private fun extractSentenceList(
        block: String,
        fieldName: String,
        maxItems: Int,
    ): List<String> {
        val pattern = Regex("\\[$fieldName\\]\\s*(.+?)(?=\\[|$)", RegexOption.DOT_MATCHES_ALL)
        val text =
            pattern.find(block)?.groupValues?.get(1)
                ?.replace("\n", " ")
                ?.trim() ?: ""
        return if (text.isNotBlank()) {
            splitIntoCompleteSentences(text).take(maxItems)
        } else {
            emptyList()
        }
    }

    private fun extractFallbackField(
        block: String,
        fieldPattern: String,
        fallback: String,
    ): String {
        val pattern = Regex("$fieldPattern[:\\s]*(.+?)(?=시장\\s*점유율|유사점|차이점|$)", RegexOption.IGNORE_CASE)
        return pattern.find(block)?.groupValues?.get(1)
            ?.replace("\n", " ")
            ?.trim()
            ?.take(SCALE_TEXT_MAX_LENGTH)
            ?: fallback
    }

    private fun extractFallbackSentenceList(
        block: String,
        fieldName: String,
        maxItems: Int,
    ): List<String> {
        val endPattern = if (fieldName == "유사점") "차이점" else "$"
        val pattern = Regex("$fieldName[:\\s]*(.+?)(?=$endPattern)", RegexOption.IGNORE_CASE)
        val text =
            pattern.find(block)?.groupValues?.get(1)
                ?.replace("\n", " ")
                ?.trim() ?: ""
        return if (text.isNotBlank()) {
            splitIntoCompleteSentences(text).take(maxItems)
        } else {
            emptyList()
        }
    }

    private data class CompetitorDetailedInfo(
        val scale: String,
        val share: String,
        val similarities: List<String>,
        val differences: List<String>,
    )

    private fun splitIntoCompleteSentences(text: String): List<String> {
        val sentences = mutableListOf<String>()

        var currentSentence = StringBuilder()
        val chars = text.toCharArray()
        var i = 0

        while (i < chars.size) {
            currentSentence.append(chars[i])

            val accumulated = currentSentence.toString()
            val endsWithTerminator = SENTENCE_TERMINATORS.any { accumulated.trimEnd().endsWith(it) }

            if (endsWithTerminator && i + 1 < chars.size && chars[i + 1] == ',') {
                val sentence = currentSentence.toString().trim()
                if (sentence.isNotBlank() && sentence.length > MIN_SENTENCE_LENGTH) {
                    sentences.add(sentence)
                }
                currentSentence = StringBuilder()
                i += 2
                if (i < chars.size && chars[i] == ' ') i++
                continue
            }

            i++
        }

        val lastSentence = currentSentence.toString().trim().removeSuffix(",").removeSuffix(".").trim()
        if (lastSentence.isNotBlank() && lastSentence.length > MIN_SENTENCE_LENGTH) {
            sentences.add(lastSentence)
        }

        return sentences
    }

    private fun parseStrengthsAnalysis(gptResponse: String): StrengthsAnalysis {
        val strengthSection = extractSection(gptResponse, "2. 강점 분석", "3. 약점 분석")

        return StrengthsAnalysis(
            competitiveAdvantages = extractListValues(strengthSection, "경쟁 우위 요소", 4),
            uniqueValuePropositions = extractListValues(strengthSection, "고유한 가치 제안", 3),
            marketOpportunities = extractListValues(strengthSection, "시장 기회 요소", 4),
            strategicRecommendations = extractListValues(strengthSection, "전략적 권고사항", 4),
        )
    }

    private fun parseWeaknessesAnalysis(gptResponse: String): WeaknessesAnalysis {
        val weaknessSection = extractSection(gptResponse, "3. 약점 분석", "4. 글로벌 진출 전략")

        return WeaknessesAnalysis(
            competitiveDisadvantages = extractListValues(weaknessSection, "경쟁 열위 요소", 4),
            marketChallenges = extractListValues(weaknessSection, "시장 도전 과제", 4),
            resourceLimitations = extractListValues(weaknessSection, "자원 제약 사항", 3),
            improvementAreas = extractListValues(weaknessSection, "개선 필요 영역", 4),
        )
    }

    private fun parseGlobalExpansionStrategy(gptResponse: String): GlobalExpansionStrategy {
        val globalSection = extractSection(gptResponse, "4. 글로벌 진출 전략", "")

        return GlobalExpansionStrategy(
            priorityMarkets = extractListValues(globalSection, "우선 진출 시장", 3),
            entryStrategies = extractListValues(globalSection, "시장별 진입 전략", 3),
            localizationRequirements = extractListValues(globalSection, "현지화 요구사항", 4),
            partnershipOpportunities = extractListValues(globalSection, "글로벌 파트너십 기회", 4),
            expectedChallenges = extractListValues(globalSection, "예상 도전 과제", 3),
        )
    }

    private fun extractSection(
        content: String,
        startMarker: String,
        endMarker: String,
    ): String {
        val possibleStartMarkers =
            listOf(
                startMarker,
                "### $startMarker",
                "##$startMarker",
                "# $startMarker",
            )

        var startIndex = -1
        for (marker in possibleStartMarkers) {
            startIndex = content.indexOf(marker, ignoreCase = true)
            if (startIndex != -1) break
        }

        if (startIndex == -1) {
            return ""
        }

        if (endMarker.isEmpty()) {
            return content.substring(startIndex)
        }

        val possibleEndMarkers =
            listOf(
                endMarker,
                "### $endMarker",
                "##$endMarker",
                "# $endMarker",
            )

        var endIndex = -1
        for (marker in possibleEndMarkers) {
            endIndex = content.indexOf(marker, startIndex + startMarker.length, ignoreCase = true)
            if (endIndex != -1) break
        }

        return if (endIndex == -1) {
            content.substring(startIndex)
        } else {
            content.substring(startIndex, endIndex)
        }
    }

    private fun extractValue(
        section: String,
        key: String,
        fallback: String,
    ): String {
        val pattern = Regex("\\[$key\\]\\s*(.+?)(?=\\[|$)", RegexOption.DOT_MATCHES_ALL)
        val match = pattern.find(section)

        if (match != null) {
            val content =
                match.groupValues[1].trim()
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
            return if (content.isNotEmpty() && content.length > 10) content else fallback
        }

        val lines = section.lines()
        val startIndex =
            lines.indexOfFirst { line ->
                val cleanLine = line.replace("**", "").trim()
                cleanLine.contains(key, ignoreCase = true)
            }
        if (startIndex == -1) return fallback

        val startLine = lines[startIndex]
        val afterColon = startLine.substringAfter(":", "").replace("**", "").trim()

        val textBuilder = StringBuilder()
        if (afterColon.isNotEmpty() && afterColon != "[" && !afterColon.startsWith("[")) {
            textBuilder.append(afterColon)
        }

        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) break
            if (line.contains(":") && !line.startsWith("-")) break
            if (line.startsWith("#")) break

            val cleanLine = line.replace("**", "").trim()
            if (cleanLine.isEmpty()) break

            textBuilder.append(" ").append(cleanLine)
        }

        val result = textBuilder.toString().trim()
        return if (result.isNotEmpty() && result.length > 10) result else fallback
    }

    private fun extractListValues(
        section: String,
        key: String,
        maxItems: Int,
    ): List<String> {
        val lines = section.lines()
        val startIndex =
            lines.indexOfFirst { line ->
                val cleanLine = line.replace("**", "").trim()
                cleanLine.contains(key, ignoreCase = true)
            }

        if (startIndex == -1) {
            return generateFallbackList(key, maxItems)
        }

        val items = mutableListOf<String>()
        var currentItem = StringBuilder()
        var inListItem = false

        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i].trim()

            if (line.isEmpty()) {
                if (currentItem.isNotEmpty()) {
                    items.add(currentItem.toString().trim())
                    currentItem = StringBuilder()
                    inListItem = false
                }
                continue
            }

            if (line.startsWith("#") || (line.contains(":") && !inListItem && !line.startsWith("-"))) {
                break
            }

            if (line.startsWith("-") || line.startsWith("•")) {
                val trimmedLine = line.removePrefix("-").removePrefix("•").trim()
                if (trimmedLine.isEmpty() || trimmedLine.all { it == '-' }) {
                    continue
                }

                if (currentItem.isNotEmpty()) {
                    items.add(currentItem.toString().trim())
                    if (items.size >= maxItems) break
                }
                currentItem = StringBuilder(trimmedLine)
                inListItem = true
            } else if (inListItem) {
                currentItem.append(" ").append(line)
            }
        }

        if (currentItem.isNotEmpty() && items.size < maxItems) {
            items.add(currentItem.toString().trim())
        }

        return items.ifEmpty { generateFallbackList(key, maxItems) }
    }

    private fun generateFallbackList(
        key: String,
        maxItems: Int,
    ): List<String> {
        val fallbackMap =
            mapOf(
                "트렌드" to listOf("디지털 전환", "지속가능성", "개인화 서비스"),
                "장벽" to listOf("자본 요구사항", "규제 환경", "기술적 복잡성"),
                "우위" to listOf("혁신적 접근", "고객 중심", "효율적 운영"),
                "열위" to listOf("시장 인지도 부족", "자원 제약", "경험 부족"),
                "니즈" to listOf("편의성 향상", "비용 절감", "개인화"),
                "공백" to listOf("틈새 시장", "서비스 공백", "지역적 공백"),
                "파트너십" to listOf("기술 파트너", "유통 파트너", "전략적 제휴"),
                "실행" to listOf("시장 조사", "프로토타입 개발", "파일럿 테스트", "마케팅 전략", "자금 조달"),
            )

        return fallbackMap.entries
            .find { key.contains(it.key) }
            ?.value
            ?.take(maxItems)
            ?: (1..maxItems).map { "분석 항목 $it" }
    }

    private fun createFallbackResponse(
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
    ): CompetitorAnalysisResponse {
        return CompetitorAnalysisResponse(
            bmcId = userBmc.id!!,
            userBmc = createFallbackUserBmcSummary(userBmc),
            userScale = createFallbackUserScale(competitors),
            strengths = createFallbackStrengths(),
            weaknesses = createFallbackWeaknesses(),
            globalExpansionStrategy = createFallbackGlobalStrategy(),
            createdAt = LocalDateTime.now(),
        )
    }

    private fun createFallbackUserBmcSummary(userBmc: BusinessModelCanvas): UserBmcSummary {
        return UserBmcSummary(
            title = userBmc.title,
            valueProposition = userBmc.valueProposition,
            targetCustomer = userBmc.customerSegments,
            keyStrengths = listOf("비즈니스 모델 수립", "명확한 비전"),
        )
    }

    private fun createFallbackUserScale(competitors: List<CompetitorInfo>): UserScaleAnalysis {
        return UserScaleAnalysis(
            estimatedUserBase = "초기 단계 스타트업",
            marketPosition = "신규 진입자",
            growthPotential = "높은 성장 가능성",
            competitorComparison = createFallbackCompetitorScales(competitors),
        )
    }

    private fun createFallbackCompetitorScales(competitors: List<CompetitorInfo>): List<CompetitorScale> {
        return competitors.take(MAX_COMPETITORS).map { competitor ->
            CompetitorScale(
                name = competitor.name,
                logoUrl = competitor.logoUrl,
                websiteUrl = competitor.websiteUrl,
                estimatedScale = FALLBACK_SCALE,
                marketShare = FALLBACK_MARKET_SHARE,
            )
        }
    }

    private fun createFallbackStrengths(): StrengthsAnalysis {
        return StrengthsAnalysis(
            competitiveAdvantages = listOf("혁신적 접근", "유연성", "빠른 적응력", "고객 중심 사고"),
            uniqueValuePropositions = listOf("차별화된 서비스", "사용자 경험 중시", "맞춤형 솔루션"),
            marketOpportunities = listOf("디지털 전환", "신규 시장 창출", "미충족 니즈 해결", "기술 혁신"),
            strategicRecommendations = listOf("시장 검증", "제품 개발", "마케팅 전략", "파트너십 구축"),
        )
    }

    private fun createFallbackWeaknesses(): WeaknessesAnalysis {
        return WeaknessesAnalysis(
            competitiveDisadvantages = listOf("브랜드 인지도 부족", "자원 제약", "시장 경험 부족", "초기 단계 리스크"),
            marketChallenges = listOf("경쟁 심화", "고객 획득 비용", "시장 진입 장벽", "규제 환경"),
            resourceLimitations = listOf("자금 제약", "인력 부족", "기술 인프라"),
            improvementAreas = listOf("마케팅 역량", "운영 효율성", "고객 서비스", "기술 개발"),
        )
    }

    private fun createFallbackGlobalStrategy(): GlobalExpansionStrategy {
        return GlobalExpansionStrategy(
            priorityMarkets = listOf("동남아시아 (베트남, 태국)", "북미 (미국, 캐나다)", "유럽 (영국, 독일)"),
            entryStrategies = listOf("모바일 우선 전략과 현지 언어 지원", "프리미엄 포지셔닝과 파트너십", "규제 준수와 현지 파트너 확보"),
            localizationRequirements = listOf("다국어 지원 (영어, 중국어 우선)", "현지 결제 시스템 통합", "문화적 맞춤화", "법규 준수"),
            partnershipOpportunities = listOf("현지 유통 채널 파트너십", "기술 플랫폼 제휴", "전략적 투자자 확보", "정부 및 공공기관 협력"),
            expectedChallenges = listOf("문화적 차이와 언어 장벽", "현지 경쟁사의 시장 선점", "규제 및 법적 제약"),
        )
    }

    private suspend fun getOrCreateTask(
        bmcId: Long,
        taskSupplier: suspend () -> CompetitorAnalysisResponse,
    ): CompetitorAnalysisResponse {
        val existingTask = ongoingTasks[bmcId]
        if (existingTask != null) {
            return existingTask.await()
        }

        val deferred = CompletableDeferred<CompetitorAnalysisResponse>()
        val previousTask = ongoingTasks.putIfAbsent(bmcId, deferred)

        if (previousTask != null) {
            return previousTask.await()
        }

        return try {
            val result = taskSupplier()
            deferred.complete(result)
            result
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            ongoingTasks.remove(bmcId)
        }
    }

    private fun getOngoingTask(bmcId: Long): Deferred<CompetitorAnalysisResponse>? = ongoingTasks[bmcId]

    private fun buildAnalysisPrompt(
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
    ): String {
        val competitorSection = buildCompetitorSection(competitors)

        return """
            당신은 비즈니스 전략 컨설턴트입니다. 아래 사용자의 BMC를 바탕으로 상세한 경쟁 분석을 수행해주세요.

            ## 사용자 BMC 정보:
            제목: ${userBmc.title}
            가치 제안: ${userBmc.valueProposition ?: "미정의"}
            고객 세그먼트: ${userBmc.customerSegments ?: "미정의"}
            채널: ${userBmc.channels ?: "미정의"}
            고객 관계: ${userBmc.customerRelationships ?: "미정의"}
            수익원: ${userBmc.revenueStreams ?: "미정의"}
            핵심 자원: ${userBmc.keyResources ?: "미정의"}
            핵심 활동: ${userBmc.keyActivities ?: "미정의"}
            핵심 파트너: ${userBmc.keyPartners ?: "미정의"}
            비용 구조: ${userBmc.costStructure ?: "미정의"}

            $competitorSection

            ## 분석 요청사항:
            다음 5개 섹션으로 **구체적이고 상세하게** 분석을 제공해주세요. 각 항목은 단순 단어가 아닌 완전한 문장으로 작성해야 합니다.

            **중요**: 각 문장에서 핵심적이고 중요한 키워드나 구절은 <<내용>> 형식으로 감싸서 강조 표시해주세요.
            예시: "고정밀 센서를 활용하여 <<오탐률을 최소화>>하는 기술은 경쟁력의 핵심입니다"

            ### 0. BMC 핵심 강점

            핵심 강점:
            - [첫 번째 BMC 핵심 강점을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 BMC 핵심 강점을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 BMC 핵심 강점을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            ### 1. 사용자 규모 분석

            [예상_사용자_기반_규모] 현재 BMC 기반으로 초기 단계인지, 성장 단계인지 등을 2-3문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조

            [시장_내_위치] 경쟁사 대비 사용자의 시장 포지셔닝을 2-3문장으로 설명. 신규 진입자인지, 차별화 전략이 있는지 등. 중요한 키워드는 <<키워드>> 형식으로 강조

            [성장_잠재력] BMC의 가치 제안과 시장 기회를 고려하여 성장 가능성을 2-3문장으로 평가. 중요한 키워드는 <<키워드>> 형식으로 강조

            [경쟁사별_분석]
            ${buildCompetitorAnalysisSection(competitors)}
            [경쟁사별_분석_끝]

            ### 2. 강점 분석

            ${if (competitors.isNotEmpty()) "주요 경쟁사: ${competitors.joinToString(", ") { it.name }}" else ""}

            경쟁 우위 요소:
            - [첫 번째 경쟁 우위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 경쟁 우위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 경쟁 우위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 경쟁 우위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            고유한 가치 제안:
            - [첫 번째 고유 가치를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 고유 가치를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 고유 가치를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            시장 기회 요소:
            - [첫 번째 시장 기회를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 시장 기회를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 시장 기회를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 시장 기회를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            전략적 권고사항:
            - [첫 번째 권고사항을 구체적인 실행 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 권고사항을 구체적인 실행 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 권고사항을 구체적인 실행 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 권고사항을 구체적인 실행 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            ### 3. 약점 분석

            경쟁 열위 요소:
            - [첫 번째 경쟁 열위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 경쟁 열위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 경쟁 열위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 경쟁 열위를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            시장 도전 과제:
            - [첫 번째 도전 과제를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 도전 과제를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 도전 과제를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 도전 과제를 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            자원 제약 사항:
            - [첫 번째 자원 제약을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 자원 제약을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 자원 제약을 1-2문장으로 구체적으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            개선 필요 영역:
            - [첫 번째 개선 영역을 구체적인 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 개선 영역을 구체적인 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 개선 영역을 구체적인 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 개선 영역을 구체적인 방안과 함께 2-3문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조]

            ### 4. 글로벌 진출 전략

            **중요**: 아래 모든 항목은 완전한 문장이 아닌 **핵심 키워드 형식**으로 **쉼표로 나누어서** 작성해주세요. 중요한 키워드는 <<키워드>> 형식으로 강조 표시해주세요.

            우선 진출 시장 (3개):
            - [첫 번째 우선 시장을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 우선 시장을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 우선 시장을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]

            시장별 진입 전략 (3개):
            - [첫 번째 시장의 진입 전략을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 시장의 진입 전략을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 시장의 진입 전략을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]

            현지화 요구사항 (3-4개):
            - [첫 번째 현지화 요구사항을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 현지화 요구사항을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 현지화 요구사항을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 현지화 요구사항을 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]

            글로벌 파트너십 기회 (3-4개):
            - [첫 번째 파트너십 기회를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 파트너십 기회를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 파트너십 기회를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [네 번째 파트너십 기회를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]

            예상 도전 과제 (3개):
            - [첫 번째 도전 과제를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [두 번째 도전 과제를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]
            - [세 번째 도전 과제를 키워드 형식으로 작성. 중요한 키워드는 <<키워드>> 형식으로 강조]

            **중요**: 각 항목은 반드시 완전한 문장으로 작성하고, 단순 키워드가 아닌 구체적인 설명과 근거를 포함해주세요. 중요한 부분은 반드시 <<내용>> 형식으로 강조 표시해주세요.
            """.trimIndent()
    }

    private fun buildCompetitorSection(competitors: List<CompetitorInfo>): String {
        return if (competitors.isEmpty()) {
            """
            ## 경쟁사 정보:
            검색된 경쟁사가 없습니다. 사용자의 BMC와 해당 산업/시장의 일반적인 경쟁 환경을 고려하여 분석해주세요.
            """.trimIndent()
        } else {
            """
            ## 발견된 실제 경쟁사:
            ${competitors.joinToString("\n") { "- ${it.name}: ${it.description}\n  웹사이트: ${it.websiteUrl}" }}
            """.trimIndent()
        }
    }

    private fun buildCompetitorAnalysisSection(competitors: List<CompetitorInfo>): String {
        return competitors.joinToString("\n") { comp ->
            """
            [경쟁사:${comp.name}]
            [예상_규모] 이 회사의 규모를 1-2문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조
            [시장_점유율] 예상 수치와 근거를 1문장으로 설명. 중요한 수치는 <<수치>> 형식으로 강조
            [유사점]
            첫 번째 유사점 문장입니다, 두 번째 유사점 문장입니다, 세 번째 유사점 문장입니다
            **형식 규칙**:
            - 반드시 쉼표(,)로만 구분
            - 각 문장은 "~합니다" 또는 "~하고 있습니다"로 끝남
            - "계시다" 같은 과도한 존칭 금지
            - 중요 키워드는 <<키워드>> 형식으로 강조
            - 예시: "<<빠른 배달 서비스>>를 제공합니다, <<저렴한 가격대>>를 유지하고 있습니다, <<지역 밀착형 서비스>>를 운영합니다"
            [차이점]
            첫 번째 차이점 문장입니다, 두 번째 차이점 문장입니다, 세 번째 차이점 문장입니다
            **형식 규칙**:
            - 반드시 쉼표(,)로만 구분
            - 각 문장은 "~합니다" 또는 "~하고 있습니다"로 끝남
            - "계시다" 같은 과도한 존칭 금지
            - 중요 키워드는 <<키워드>> 형식으로 강조
            - 예시: "한식 전문이 아닌 <<다양한 음식점>>을 포함합니다, 도시락 중심이 아닌 <<전반적인 배달 지원>>을 제공합니다, <<대기업 기반의 대규모 인프라>>를 보유하고 있습니다"
            [경쟁사_끝:${comp.name}]
            """.trimIndent()
        }
    }
}
