package com.jininsadaecheonmyeong.starthubserver.domain.analysis.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorInfo
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorScale
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.GlobalExpansionStrategy
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.StrengthsAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.UserBmcSummary
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.UserScaleAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.WeaknessesAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.entity.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.repository.CompetitorAnalysisRepository
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BmcNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.PerplexitySearchService
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchRequest
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CompetitorAnalysisService(
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val competitorAnalysisRepository: CompetitorAnalysisRepository,
    private val perplexitySearchService: PerplexitySearchService,
    private val chatModel: ChatModel,
    private val promptBuilder: CompetitorAnalysisPromptBuilder,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(CompetitorAnalysisService::class.java)

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
    fun analyzeCompetitors(request: CompetitorAnalysisRequest): CompetitorAnalysisResponse {
        val user = userAuthenticationHolder.current()
        val userBmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        validateUserAccess(userBmc, user)

        val existingAnalysis = competitorAnalysisRepository.findByBusinessModelCanvasAndDeletedFalse(userBmc)
        if (existingAnalysis.isPresent) {
            return deserializeAnalysisResponse(existingAnalysis.get())
        }

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
                val analysisPrompt = promptBuilder.buildAnalysisPrompt(userBmc, competitors)
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
                .orElseThrow { throw IllegalStateException("저장된 경쟁사 분석을 찾을 수 없습니다.") }

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
            throw BmcNotFoundException("접근 권한이 없습니다.")
        }
    }

    private fun generateSearchKeywords(userBmc: BusinessModelCanvas): List<String> {
        val allText = listOfNotNull(userBmc.title, userBmc.valueProposition, userBmc.customerSegments).joinToString(" ")

        val keywords =
            allText
                .replace("\n", " ")
                .replace("-", " ")
                .split(Regex("[,\\.;]"))
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
                if (keyStrengths.isNotEmpty()) {
                    keyStrengths
                } else {
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
        val pattern = Regex("\\[$fieldName\\]\\s*(.+?)(?=\\[|$)", RegexOption.DOT_MATCHES_ALL)
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
            logger.warn("Section start marker '{}' not found", startMarker)
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
            logger.warn("Key '{}' not found in section", key)
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
}
