package com.jininsadaecheonmyeong.starthubserver.global.infra.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.jininsadaecheonmyeong.starthubserver.global.config.PerplexityProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.exception.SearchException
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.SearchRequest
import kotlinx.coroutines.reactive.awaitSingle
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
) {
    private val logger = LoggerFactory.getLogger(PerplexitySearchService::class.java)

    suspend fun searchCompetitors(request: SearchRequest): List<String> {
        if (properties.apiKey.isBlank() || properties.apiKey.startsWith("\${")) {
            logger.error("Perplexity API 키가 설정되지 않음")
            throw SearchException("Perplexity API Key를 찾을 수 없음")
        }

        return try {
            val searchQuery = buildCompetitorSearchQuery(request)
            val response = performSearchSuspend(searchQuery)
            parseCompanyNames(response)
        } catch (e: Exception) {
            logger.error("경쟁사 검색 실패: {}", e.message)
            when {
                e.message?.contains("API key") == true ->
                    throw SearchException("Perplexity API 키가 유효하지 않음.", e)
                e.message?.contains("rate limit") == true ->
                    throw SearchException("Perplexity API 사용량 제한 초과. 잠시 후 다시 시도해주세요.", e)
                else ->
                    throw SearchException("경쟁사 검색 실패: ${e.message}", e)
            }
        }
    }

    private fun buildCompetitorSearchQuery(request: SearchRequest): String {
        val bmcContext = request.bmcContext

        return if (bmcContext != null) {
            """
            다음 스타트업/비즈니스의 직접적인 경쟁사 회사명을 정확히 4개만 알려주세요 (국내 2개, 해외 2개):

            - 이름: ${bmcContext.title}
            - 가치 제안: ${bmcContext.valueProposition ?: "정보 없음"}
            - 목표 고객: ${bmcContext.customerSegments ?: "정보 없음"}
            - 채널: ${bmcContext.channels ?: "정보 없음"}
            - 수익 모델: ${bmcContext.revenueStreams ?: "정보 없음"}

            조건:
            - 중소기업, 스타트업, 중견기업 우선 (대기업 제외)
            - 비슷한 고객층과 서비스를 가진 직접 경쟁사
            - 실제로 존재하는 회사만

            응답 형식: 회사명만 쉼표로 구분하여 한 줄로 작성 (예: 회사A, 회사B, 회사C, 회사D)
            """.trimIndent()
        } else {
            """
            다음 키워드와 관련된 경쟁 회사명을 정확히 4개만 알려주세요 (국내 2개, 해외 2개): "${request.query}"

            조건: 중소기업/스타트업 우선, 대기업 제외, 실제 존재하는 회사만

            응답 형식: 회사명만 쉼표로 구분하여 한 줄로 작성 (예: 회사A, 회사B, 회사C, 회사D)
            """.trimIndent()
        }
    }

    private suspend fun performSearchSuspend(query: String): PerplexityResponse {
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
                                반드시 실제로 존재하는 회사만 추천하세요.
                                대기업은 제외하고 중소기업, 스타트업, 중견기업을 찾으세요.
                                회사명만 쉼표로 구분하여 응답하세요. 부가 설명 없이 회사명만 작성하세요.
                                """.trimIndent(),
                        ),
                        PerplexityMessage(
                            role = "user",
                            content = query,
                        ),
                    ),
                maxTokens = properties.maxTokens,
                temperature = 0.2,
                searchRecencyFilter = "month",
                returnCitations = true,
            )

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
                logger.error("Perplexity API 오류: 상태코드 {}", ex.statusCode.value())
                when (ex.statusCode.value()) {
                    401 -> SearchException("Perplexity API 키가 유효하지 않음.", ex)
                    429 -> SearchException("Perplexity API 사용량 제한 초과", ex)
                    400 -> SearchException("잘못된 검색 파라미터", ex)
                    else -> SearchException("Perplexity API 오류: ${ex.statusText}", ex)
                }
            }
            .awaitSingle()
    }

    suspend fun searchWeb(query: String): List<WebSearchResult> {
        if (properties.apiKey.isBlank() || properties.apiKey.startsWith("\${")) {
            return emptyList()
        }

        return try {
            val requestBody =
                PerplexityRequest(
                    model = properties.model,
                    messages =
                        listOf(
                            PerplexityMessage(role = "system", content = "웹 검색 결과를 요약하여 제공하세요."),
                            PerplexityMessage(role = "user", content = query),
                        ),
                    maxTokens = properties.maxTokens,
                    temperature = 0.3,
                    returnCitations = true,
                )

            val response =
                webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PerplexityResponse::class.java)
                    .timeout(Duration.ofMillis(properties.timeout))
                    .awaitSingle()

            val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()
            val citations = response.citations ?: emptyList()

            listOf(
                WebSearchResult(
                    title = "검색 결과",
                    url = citations.firstOrNull() ?: "",
                    snippet = content.take(1000),
                ),
            )
        } catch (e: Exception) {
            logger.warn("웹 검색 실패: {}", e.message)
            emptyList()
        }
    }

    data class WebSearchResult(
        val title: String,
        val url: String,
        val snippet: String,
    )

    private fun parseCompanyNames(response: PerplexityResponse): List<String> {
        val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()

        return content
            .replace("**", "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 1 }
            .take(4)
    }

    data class PerplexityRequest(
        val model: String,
        val messages: List<PerplexityMessage>,
        @JsonProperty("max_tokens")
        val maxTokens: Int,
        val temperature: Double,
        @JsonProperty("search_recency_filter")
        val searchRecencyFilter: String? = null,
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
