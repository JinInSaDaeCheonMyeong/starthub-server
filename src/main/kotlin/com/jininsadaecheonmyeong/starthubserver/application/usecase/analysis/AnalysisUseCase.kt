package com.jininsadaecheonmyeong.starthubserver.application.usecase.analysis

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.UserContextService
import com.jininsadaecheonmyeong.starthubserver.domain.entity.analysis.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.exception.analysis.BmcAccessDeniedException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.analysis.CompetitorAnalysisNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.bmc.BmcNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.analysis.CompetitorAnalysisRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.CompanyInfoService
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.GoogleSearchService
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
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.ResponseFormat
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
    private val googleSearchService: GoogleSearchService,
    private val companyInfoService: CompanyInfoService,
    private val userContextService: UserContextService,
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
                val companyNames = perplexitySearchService.searchCompetitors(searchRequest)
                logger.info("Perplexity 경쟁사 식별 완료: {}", companyNames)

                val searchResults = googleSearchService.searchCompanies(companyNames)
                logger.info("Google Search 검증 완료: {}건", searchResults.size)

                searchResults.map { result ->
                    val domain = result.url.substringAfter("://").substringBefore("/").removePrefix("www.")
                    val logoUrl =
                        try {
                            companyInfoService.getLogoUrl(domain)
                        } catch (e: Exception) {
                            logger.warn("로고 조회 실패 - domain: {}", domain)
                            null
                        }

                    CompetitorInfo(
                        name = result.title,
                        description = result.snippet,
                        logoUrl = logoUrl,
                        websiteUrl = result.url,
                    )
                }
            } catch (e: Exception) {
                logger.error("경쟁사 검색 실패: {}", e.message)
                emptyList()
            }

        val analysisResponse =
            try {
                val promptText = buildAnalysisPrompt(userBmc, competitors)
                val gptResult =
                    withContext(Dispatchers.IO) {
                        callGptWithJsonMode(promptText)
                    }
                mapToResponse(userBmc, competitors, gptResult)
            } catch (e: Exception) {
                logger.error("경쟁사 분석 실패: {}", e.message)
                createFallbackResponse(userBmc, competitors)
            }
        withContext(Dispatchers.IO) {
            saveAnalysis(user, userBmc, analysisResponse)
        }
        return analysisResponse
    }

    private fun callGptWithJsonMode(promptText: String): GptAnalysisJsonResponse {
        val options =
            OpenAiChatOptions.builder()
                .responseFormat(ResponseFormat(ResponseFormat.Type.JSON_OBJECT, ""))
                .build()
        val prompt = Prompt(promptText, options)
        val response = chatModel.call(prompt)
        return objectMapper.readValue(response.result.output.text, GptAnalysisJsonResponse::class.java)
    }

    private fun mapToResponse(
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
        gpt: GptAnalysisJsonResponse,
    ): CompetitorAnalysisResponse {
        val competitorScales =
            competitors.take(MAX_COMPETITORS).mapIndexed { index, competitor ->
                val gptComp = gpt.competitorScales.getOrNull(index)
                CompetitorScale(
                    name = competitor.name,
                    logoUrl = competitor.logoUrl,
                    websiteUrl = competitor.websiteUrl,
                    estimatedScale = gptComp?.estimatedScale ?: "중간 규모",
                    marketShare = gptComp?.marketShare ?: "5-10%",
                    similarities = gptComp?.similarities ?: emptyList(),
                    differences = gptComp?.differences ?: emptyList(),
                )
            }

        return CompetitorAnalysisResponse(
            bmcId = userBmc.id!!,
            userBmc =
                UserBmcSummary(
                    title = userBmc.title,
                    valueProposition = userBmc.valueProposition,
                    targetCustomer = userBmc.customerSegments,
                    keyStrengths =
                        gpt.userBmcSummary.keyStrengths.ifEmpty {
                            extractKeyStrengthsFallback(userBmc)
                        },
                ),
            userScale =
                UserScaleAnalysis(
                    estimatedUserBase = gpt.userScaleAnalysis.estimatedUserBase,
                    marketPosition = gpt.userScaleAnalysis.marketPosition,
                    growthPotential = gpt.userScaleAnalysis.growthPotential,
                    competitorComparison = competitorScales,
                ),
            strengths =
                StrengthsAnalysis(
                    competitiveAdvantages = gpt.strengthsAnalysis.competitiveAdvantages,
                    uniqueValuePropositions = gpt.strengthsAnalysis.uniqueValuePropositions,
                    marketOpportunities = gpt.strengthsAnalysis.marketOpportunities,
                    strategicRecommendations = gpt.strengthsAnalysis.strategicRecommendations,
                ),
            weaknesses =
                WeaknessesAnalysis(
                    competitiveDisadvantages = gpt.weaknessesAnalysis.competitiveDisadvantages,
                    marketChallenges = gpt.weaknessesAnalysis.marketChallenges,
                    resourceLimitations = gpt.weaknessesAnalysis.resourceLimitations,
                    improvementAreas = gpt.weaknessesAnalysis.improvementAreas,
                ),
            globalExpansionStrategy =
                GlobalExpansionStrategy(
                    priorityMarkets = gpt.globalExpansionStrategy.priorityMarkets,
                    entryStrategies = gpt.globalExpansionStrategy.entryStrategies,
                    localizationRequirements = gpt.globalExpansionStrategy.localizationRequirements,
                    partnershipOpportunities = gpt.globalExpansionStrategy.partnershipOpportunities,
                    expectedChallenges = gpt.globalExpansionStrategy.expectedChallenges,
                ),
            createdAt = LocalDateTime.now(),
        )
    }

    private fun buildAnalysisPrompt(
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
    ): String {
        val competitorSection =
            if (competitors.isEmpty()) {
                "검색된 경쟁사가 없습니다. 사용자의 BMC와 해당 산업/시장의 일반적인 경쟁 환경을 고려하여 분석해주세요."
            } else {
                competitors.joinToString("\n") { "- ${it.name}: ${it.description}\n  웹사이트: ${it.websiteUrl}" }
            }

        val competitorScaleInstruction =
            if (competitors.isEmpty()) {
                "\"competitorScales\": []"
            } else {
                """
                "competitorScales": [
                    ${competitors.joinToString(",\n") { comp ->
                    """  {
                    "name": "${comp.name}",
                    "estimatedScale": "이 회사의 규모를 1-2문장으로 설명. 중요한 키워드는 <<키워드>> 형식으로 강조",
                    "marketShare": "예상 수치와 근거를 1문장으로 설명",
                    "similarities": ["유사점 1 (완전한 문장)", "유사점 2", "유사점 3"],
                    "differences": ["차이점 1 (완전한 문장)", "차이점 2", "차이점 3"]
                  }"""
                }}
                ]
                """.trimIndent()
            }

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

            ## 경쟁사 정보:
            $competitorSection

            ## 응답 형식:
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            각 문장에서 핵심적이고 중요한 키워드나 구절은 <<내용>> 형식으로 감싸서 강조 표시해주세요.

            {
              "userBmcSummary": {
                "keyStrengths": ["BMC 핵심 강점 1 (1-2문장, <<키워드>> 강조)", "핵심 강점 2", "핵심 강점 3"]
              },
              "userScaleAnalysis": {
                "estimatedUserBase": "현재 BMC 기반 규모를 2-3문장으로 설명. <<키워드>> 강조",
                "marketPosition": "경쟁사 대비 시장 포지셔닝 2-3문장. <<키워드>> 강조",
                "growthPotential": "성장 가능성 2-3문장. <<키워드>> 강조"
              },
              $competitorScaleInstruction,
              "strengthsAnalysis": {
                "competitiveAdvantages": ["경쟁 우위 1 (1-2문장, <<키워드>> 강조)", "경쟁 우위 2", "경쟁 우위 3", "경쟁 우위 4"],
                "uniqueValuePropositions": ["고유 가치 1", "고유 가치 2", "고유 가치 3"],
                "marketOpportunities": ["시장 기회 1", "시장 기회 2", "시장 기회 3", "시장 기회 4"],
                "strategicRecommendations": ["전략적 권고 1 (2-3문장)", "전략적 권고 2", "전략적 권고 3", "전략적 권고 4"]
              },
              "weaknessesAnalysis": {
                "competitiveDisadvantages": ["경쟁 열위 1 (1-2문장, <<키워드>> 강조)", "경쟁 열위 2", "경쟁 열위 3", "경쟁 열위 4"],
                "marketChallenges": ["시장 도전 1", "시장 도전 2", "시장 도전 3", "시장 도전 4"],
                "resourceLimitations": ["자원 제약 1", "자원 제약 2", "자원 제약 3"],
                "improvementAreas": ["개선 영역 1 (2-3문장)", "개선 영역 2", "개선 영역 3", "개선 영역 4"]
              },
              "globalExpansionStrategy": {
                "priorityMarkets": ["우선 시장 1", "우선 시장 2", "우선 시장 3"],
                "entryStrategies": ["진입 전략 1", "진입 전략 2", "진입 전략 3"],
                "localizationRequirements": ["현지화 요구 1", "현지화 요구 2", "현지화 요구 3", "현지화 요구 4"],
                "partnershipOpportunities": ["파트너십 기회 1", "파트너십 기회 2", "파트너십 기회 3", "파트너십 기회 4"],
                "expectedChallenges": ["도전 과제 1", "도전 과제 2", "도전 과제 3"]
              }
            }

            **중요**: 각 항목은 반드시 완전한 문장으로 작성하고, 단순 키워드가 아닌 구체적인 설명과 근거를 포함해주세요. 중요한 부분은 반드시 <<내용>> 형식으로 강조 표시해주세요.
            """.trimIndent()
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

        userContextService.embedUserContextAsync(user)
        logger.info("경쟁사분석 저장으로 인한 사용자 컨텍스트 임베딩 트리거 - BMC ID: {}, User ID: {}", bmc.id, user.id)
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

    private fun extractKeyStrengthsFallback(userBmc: BusinessModelCanvas): List<String> {
        return buildList {
            if (!userBmc.valueProposition.isNullOrBlank()) add("명확한 가치 제안")
            if (!userBmc.keyResources.isNullOrBlank()) add("핵심 자원 보유")
            if (!userBmc.revenueStreams.isNullOrBlank()) add("수익 모델 확립")
        }.ifEmpty { listOf("비즈니스 모델 수립 완료") }
    }

    private fun createFallbackResponse(
        userBmc: BusinessModelCanvas,
        competitors: List<CompetitorInfo>,
    ): CompetitorAnalysisResponse {
        return CompetitorAnalysisResponse(
            bmcId = userBmc.id!!,
            userBmc =
                UserBmcSummary(
                    title = userBmc.title,
                    valueProposition = userBmc.valueProposition,
                    targetCustomer = userBmc.customerSegments,
                    keyStrengths = listOf("비즈니스 모델 수립", "명확한 비전"),
                ),
            userScale =
                UserScaleAnalysis(
                    estimatedUserBase = "초기 단계 스타트업",
                    marketPosition = "신규 진입자",
                    growthPotential = "높은 성장 가능성",
                    competitorComparison =
                        competitors.take(MAX_COMPETITORS).map { competitor ->
                            CompetitorScale(
                                name = competitor.name,
                                logoUrl = competitor.logoUrl,
                                websiteUrl = competitor.websiteUrl,
                                estimatedScale = "중간 규모",
                                marketShare = "5-10%",
                            )
                        },
                ),
            strengths =
                StrengthsAnalysis(
                    competitiveAdvantages = listOf("혁신적 접근", "유연성", "빠른 적응력", "고객 중심 사고"),
                    uniqueValuePropositions = listOf("차별화된 서비스", "사용자 경험 중시", "맞춤형 솔루션"),
                    marketOpportunities = listOf("디지털 전환", "신규 시장 창출", "미충족 니즈 해결", "기술 혁신"),
                    strategicRecommendations = listOf("시장 검증", "제품 개발", "마케팅 전략", "파트너십 구축"),
                ),
            weaknesses =
                WeaknessesAnalysis(
                    competitiveDisadvantages = listOf("브랜드 인지도 부족", "자원 제약", "시장 경험 부족", "초기 단계 리스크"),
                    marketChallenges = listOf("경쟁 심화", "고객 획득 비용", "시장 진입 장벽", "규제 환경"),
                    resourceLimitations = listOf("자금 제약", "인력 부족", "기술 인프라"),
                    improvementAreas = listOf("마케팅 역량", "운영 효율성", "고객 서비스", "기술 개발"),
                ),
            globalExpansionStrategy =
                GlobalExpansionStrategy(
                    priorityMarkets = listOf("동남아시아 (베트남, 태국)", "북미 (미국, 캐나다)", "유럽 (영국, 독일)"),
                    entryStrategies = listOf("모바일 우선 전략과 현지 언어 지원", "프리미엄 포지셔닝과 파트너십", "규제 준수와 현지 파트너 확보"),
                    localizationRequirements = listOf("다국어 지원 (영어, 중국어 우선)", "현지 결제 시스템 통합", "문화적 맞춤화", "법규 준수"),
                    partnershipOpportunities = listOf("현지 유통 채널 파트너십", "기술 플랫폼 제휴", "전략적 투자자 확보", "정부 및 공공기관 협력"),
                    expectedChallenges = listOf("문화적 차이와 언어 장벽", "현지 경쟁사의 시장 선점", "규제 및 법적 제약"),
                ),
            createdAt = LocalDateTime.now(),
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

    data class GptAnalysisJsonResponse(
        @JsonProperty("userBmcSummary")
        val userBmcSummary: GptUserBmcSummary = GptUserBmcSummary(),
        @JsonProperty("userScaleAnalysis")
        val userScaleAnalysis: GptUserScaleAnalysis = GptUserScaleAnalysis(),
        @JsonProperty("competitorScales")
        val competitorScales: List<GptCompetitorScale> = emptyList(),
        @JsonProperty("strengthsAnalysis")
        val strengthsAnalysis: GptStrengthsAnalysis = GptStrengthsAnalysis(),
        @JsonProperty("weaknessesAnalysis")
        val weaknessesAnalysis: GptWeaknessesAnalysis = GptWeaknessesAnalysis(),
        @JsonProperty("globalExpansionStrategy")
        val globalExpansionStrategy: GptGlobalExpansionStrategy = GptGlobalExpansionStrategy(),
    )

    data class GptUserBmcSummary(
        @JsonProperty("keyStrengths")
        val keyStrengths: List<String> = emptyList(),
    )

    data class GptUserScaleAnalysis(
        @JsonProperty("estimatedUserBase")
        val estimatedUserBase: String = "초기 단계 스타트업",
        @JsonProperty("marketPosition")
        val marketPosition: String = "신규 진입자",
        @JsonProperty("growthPotential")
        val growthPotential: String = "높은 성장 가능성",
    )

    data class GptCompetitorScale(
        @JsonProperty("name")
        val name: String = "",
        @JsonProperty("estimatedScale")
        val estimatedScale: String = "중간 규모",
        @JsonProperty("marketShare")
        val marketShare: String = "5-10%",
        @JsonProperty("similarities")
        val similarities: List<String> = emptyList(),
        @JsonProperty("differences")
        val differences: List<String> = emptyList(),
    )

    data class GptStrengthsAnalysis(
        @JsonProperty("competitiveAdvantages")
        val competitiveAdvantages: List<String> = emptyList(),
        @JsonProperty("uniqueValuePropositions")
        val uniqueValuePropositions: List<String> = emptyList(),
        @JsonProperty("marketOpportunities")
        val marketOpportunities: List<String> = emptyList(),
        @JsonProperty("strategicRecommendations")
        val strategicRecommendations: List<String> = emptyList(),
    )

    data class GptWeaknessesAnalysis(
        @JsonProperty("competitiveDisadvantages")
        val competitiveDisadvantages: List<String> = emptyList(),
        @JsonProperty("marketChallenges")
        val marketChallenges: List<String> = emptyList(),
        @JsonProperty("resourceLimitations")
        val resourceLimitations: List<String> = emptyList(),
        @JsonProperty("improvementAreas")
        val improvementAreas: List<String> = emptyList(),
    )

    data class GptGlobalExpansionStrategy(
        @JsonProperty("priorityMarkets")
        val priorityMarkets: List<String> = emptyList(),
        @JsonProperty("entryStrategies")
        val entryStrategies: List<String> = emptyList(),
        @JsonProperty("localizationRequirements")
        val localizationRequirements: List<String> = emptyList(),
        @JsonProperty("partnershipOpportunities")
        val partnershipOpportunities: List<String> = emptyList(),
        @JsonProperty("expectedChallenges")
        val expectedChallenges: List<String> = emptyList(),
    )
}
