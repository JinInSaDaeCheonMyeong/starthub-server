package com.jininsadaecheonmyeong.starthubserver.global.infra.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.jininsadaecheonmyeong.starthubserver.global.config.PerplexityProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.exception.SearchException
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.CompetitorSearchResult
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.EnhancedSearchResult
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchRequest
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchResultType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class PerplexitySearchService(
    @Qualifier("perplexityWebClient")
    private val webClient: WebClient,
    private val properties: PerplexityProperties,
    private val companyInfoService: CompanyInfoService,
) {
    private val logger = LoggerFactory.getLogger(PerplexitySearchService::class.java)

    fun searchCompetitors(request: SearchRequest): List<CompetitorSearchResult> {
        if (properties.apiKey.isBlank() || properties.apiKey.startsWith("\${")) {
            logger.error("Perplexity API key is not configured. Please set PERPLEXITY_API_KEY environment variable.")
            throw SearchException("Perplexity API is not configured. Please set PERPLEXITY_API_KEY in your environment.")
        }

        return try {
            val searchQuery = buildCompetitorSearchQuery(request)
            logger.info("=== Perplexity Search Query ===")
            logger.info(searchQuery)
            logger.info("=== End Query ===")

            val response = performSearch(searchQuery)

            logger.info("=== Perplexity Raw Response ===")
            logger.info("Response ID: {}", response.id)
            logger.info("Model: {}", response.model)
            logger.info("Citations: {}", response.citations)
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            logger.info("Content length: {} characters", content.length)
            logger.info("Content: {}", content)
            logger.info("=== End Response ===")

            val results = parseCompetitorResults(response, request.maxResults)

            logger.info("Perplexity search completed: query='{}', results={}", request.query, results.size)

            results
        } catch (e: Exception) {
            logger.error("Perplexity search failed for query: '{}', error: {}", request.query, e.message)
            logger.error("Exception details: ", e)
            when {
                e.message?.contains("API key") == true ->
                    throw SearchException("Perplexity API key is invalid. Please check your API key configuration.", e)
                e.message?.contains("rate limit") == true ->
                    throw SearchException("Perplexity API rate limit exceeded. Please try again later.", e)
                else ->
                    throw SearchException("Failed to search competitors via Perplexity: ${e.message}", e)
            }
        }
    }

    fun searchWithEnhancedResults(request: SearchRequest): List<EnhancedSearchResult> {
        val basicResults = searchCompetitors(request)

        return basicResults.map { result ->
            EnhancedSearchResult(
                basicResult = result,
                type = determineResultType(result),
                businessRelevance = calculateBusinessRelevance(result),
                companyInfo = null,
            )
        }
    }

    private fun buildCompetitorSearchQuery(request: SearchRequest): String {
        val bmcContext = request.bmcContext

        return if (bmcContext != null) {
            """
            다음 스타트업/비즈니스의 직접적인 경쟁사를 정확히 ${request.maxResults}개 찾아주세요:

            ## 우리 비즈니스 정보:
            - 이름: ${bmcContext.title}
            - 가치 제안: ${bmcContext.valueProposition ?: "정보 없음"}
            - 목표 고객: ${bmcContext.customerSegments ?: "정보 없음"}
            - 채널: ${bmcContext.channels ?: "정보 없음"}
            - 수익 모델: ${bmcContext.revenueStreams ?: "정보 없음"}

            ## 검색 조건:
            - **중소기업, 스타트업, 또는 중견기업**을 우선적으로 찾아주세요
            - **대기업(삼성, LG, 네이버, 카카오 등)은 제외**해주세요
            - 같은 산업/분야에서 **비슷한 규모의 회사**를 찾아주세요
            - 우리와 **비슷한 고객층**을 타겟으로 하는 회사를 찾아주세요
            - **한국 또는 글로벌 기업** 모두 가능하지만, 실제 웹사이트가 있는 회사만 포함해주세요
            - 우리의 가치 제안과 **직접적으로 경쟁**하는 서비스/제품을 제공하는 회사를 찾아주세요

            ## 응답 형식:
            [회사 1]
            회사명: [정확한 공식 회사명]
            웹사이트: [공식 웹사이트 URL - https:// 포함]
            설명: [100-200자 이내로 핵심 서비스/제품 설명. 우리와 어떤 점에서 경쟁하는지 명시]
            로고: [로고 이미지 URL 또는 "정보 없음"]

            [회사 2]
            회사명:
            웹사이트:
            설명:
            로고:

            [회사 3]
            회사명:
            웹사이트:
            설명:
            로고:

            **중요**: 반드시 실제로 존재하는 회사만 포함하고, 정확한 웹사이트 URL을 제공해주세요.
            """.trimIndent()
        } else {
            """
            다음 키워드와 관련된 중소/중견 규모의 실제 경쟁 회사를 정확히 ${request.maxResults}개 찾아주세요: "${request.query}"

            ## 검색 조건:
            - **중소기업, 스타트업, 또는 중견기업** 우선
            - **대기업은 제외**
            - 실제 웹사이트가 있는 회사만 포함

            ## 응답 형식:
            [회사 1]
            회사명: [정확한 공식 회사명]
            웹사이트: [공식 웹사이트 URL - https:// 포함]
            설명: [100-200자 이내로 핵심 서비스/제품 설명]
            로고: [로고 이미지 URL 또는 "정보 없음"]

            **중요**: 반드시 실제로 존재하는 회사만 포함하고, 정확한 웹사이트 URL을 제공해주세요.
            """.trimIndent()
        }
    }

    private fun performSearch(query: String): PerplexityResponse {
        val requestBody =
            PerplexityRequest(
                model = properties.model,
                messages =
                    listOf(
                        PerplexityMessage(
                            role = "system",
                            content =
                                """
                                당신은 스타트업과 중소기업 시장 분석 전문가입니다.

                                규칙:
                                1. 반드시 실제로 존재하는 회사만 추천하세요
                                2. 대기업(삼성, LG, 네이버, 카카오 등)은 제외하세요
                                3. 중소기업, 스타트업, 중견기업을 우선적으로 찾으세요
                                4. 정확한 웹사이트 URL을 제공하세요
                                5. 비슷한 규모와 타겟 고객을 가진 직접 경쟁사를 찾으세요
                                """.trimIndent(),
                        ),
                        PerplexityMessage(
                            role = "user",
                            content = query,
                        ),
                    ),
                maxTokens = properties.maxTokens,
                temperature = 0.2,
                searchDomainFilter = null,
                returnCitations = true,
            )

        logger.info("Perplexity request: model={}, maxTokens={}, temperature={}", properties.model, properties.maxTokens, 0.2)

        return webClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(PerplexityResponse::class.java)
            .timeout(Duration.ofMillis(properties.timeout))
            .retryWhen(
                Retry.backoff(2, Duration.ofSeconds(1))
                    .filter { it is TimeoutException },
            )
            .onErrorMap(WebClientResponseException::class.java) { ex ->
                logger.error("Perplexity API error response body: {}", ex.responseBodyAsString)
                when (ex.statusCode.value()) {
                    401 -> SearchException("Perplexity API key is invalid", ex)
                    429 -> SearchException("Perplexity API rate limit exceeded", ex)
                    400 -> SearchException("Invalid search parameters: ${ex.responseBodyAsString}", ex)
                    else -> SearchException("Perplexity API error: ${ex.statusText}", ex)
                }
            }
            .block() ?: throw SearchException("No response from Perplexity API")
    }

    private fun parseCompetitorResults(
        response: PerplexityResponse,
        maxResults: Int,
    ): List<CompetitorSearchResult> {
        val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()

        val competitors = mutableListOf<CompetitorSearchResult>()
        val companyBlocks = content.split(Regex("\\[회사 \\d+\\]")).filter { it.isNotBlank() }

        for (block in companyBlocks.take(maxResults)) {
            try {
                val name = extractField(block, "회사명").replace("**", "").trim()
                val website = extractField(block, "웹사이트").replace("**", "").trim()
                val description = extractField(block, "설명").replace("**", "").trim()
                val logo = extractField(block, "로고").replace("**", "").trim()

                // 잘못된 URL 필터링 (정보 없음, ## 등)
                val cleanWebsite =
                    website
                        .replace("##", "")
                        .split("\n").firstOrNull()?.trim() ?: ""

                if (name.isNotBlank() && cleanWebsite.isNotBlank() &&
                    !cleanWebsite.contains("정보 없음") &&
                    (cleanWebsite.startsWith("http://") || cleanWebsite.startsWith("https://"))
                ) {
                    competitors.add(
                        CompetitorSearchResult(
                            title = name,
                            url = normalizeUrl(cleanWebsite),
                            snippet = description.take(300),
                            displayUrl = extractDomain(cleanWebsite),
                            thumbnailUrl = if (logo.isNotBlank() && !logo.contains("없음")) logo else null,
                            relevanceScore = 0.8,
                        ),
                    )
                }
            } catch (e: Exception) {
                logger.warn("Failed to parse company block: {}", e.message)
            }
        }

        // CompanyInfoService를 사용하여 실제 웹사이트에서 로고 추출
        val enhancedCompetitors =
            competitors.map { competitor ->
                if (competitor.thumbnailUrl == null || !isValidImageUrl(competitor.thumbnailUrl)) {
                    try {
                        val extractedLogo = companyInfoService.extractCompanyLogo(competitor.url)
                        if (extractedLogo != null) {
                            logger.info("Logo extracted for {}: {}", competitor.title, extractedLogo)
                            competitor.copy(thumbnailUrl = extractedLogo)
                        } else {
                            competitor
                        }
                    } catch (e: Exception) {
                        logger.warn("Failed to extract logo for {}: {}", competitor.title, e.message)
                        competitor
                    }
                } else {
                    competitor
                }
            }

        return enhancedCompetitors
    }

    private fun extractField(
        text: String,
        fieldName: String,
    ): String {
        val pattern = Regex("$fieldName\\s*:\\s*(.+?)(?=\\n[가-힣]+:|$)", RegexOption.DOT_MATCHES_ALL)
        return pattern.find(text)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun normalizeUrl(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https://$url"
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val normalized = normalizeUrl(url)
            val domain = normalized.substringAfter("://").substringBefore("/")
            domain
        } catch (e: Exception) {
            url
        }
    }

    private fun extractBaseUrl(url: String): String {
        return try {
            val normalized = normalizeUrl(url)
            val domain = normalized.substringAfter("://").substringBefore("/")
            "https://$domain"
        } catch (e: Exception) {
            url
        }
    }

    private fun determineResultType(result: CompetitorSearchResult): SearchResultType {
        val domain = result.domain.lowercase()
        val title = result.title.lowercase()
        val snippet = result.snippet.lowercase()

        return when {
            domain.contains("linkedin") || domain.contains("facebook") -> SearchResultType.SOCIAL_MEDIA
            title.contains("news") || snippet.contains("뉴스") -> SearchResultType.NEWS_ARTICLE
            snippet.contains("제품") || snippet.contains("product") -> SearchResultType.PRODUCT_PAGE
            snippet.contains("회사") || snippet.contains("company") -> SearchResultType.COMPANY_PROFILE
            else -> SearchResultType.BUSINESS_WEBSITE
        }
    }

    private fun calculateBusinessRelevance(result: CompetitorSearchResult): Double {
        var relevance = result.relevanceScore

        when (determineResultType(result)) {
            SearchResultType.BUSINESS_WEBSITE -> relevance += 0.3
            SearchResultType.COMPANY_PROFILE -> relevance += 0.25
            SearchResultType.PRODUCT_PAGE -> relevance += 0.2
            SearchResultType.SOCIAL_MEDIA -> relevance -= 0.1
            SearchResultType.NEWS_ARTICLE -> relevance -= 0.05
            SearchResultType.OTHER -> relevance += 0.0
        }

        return relevance.coerceIn(0.0, 1.0)
    }

    private fun isValidImageUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowercaseUrl = url.lowercase()
        val validExtensions = listOf(".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".ico")
        return validExtensions.any { lowercaseUrl.contains(it) } &&
            (url.startsWith("http://") || url.startsWith("https://"))
    }

    // Perplexity API Data Classes
    data class PerplexityRequest(
        val model: String,
        val messages: List<PerplexityMessage>,
        @JsonProperty("max_tokens")
        val maxTokens: Int,
        val temperature: Double,
        @JsonProperty("search_domain_filter")
        val searchDomainFilter: List<String>? = null,
        @JsonProperty("return_citations")
        val returnCitations: Boolean = true,
    )

    data class PerplexityMessage(
        val role: String,
        val content: String,
    )

    data class PerplexityResponse(
        val id: String,
        val model: String,
        val choices: List<PerplexityChoice>,
        val citations: List<String>? = null,
    )

    data class PerplexityChoice(
        val index: Int,
        val message: PerplexityMessage,
        @JsonProperty("finish_reason")
        val finishReason: String,
    )
}
