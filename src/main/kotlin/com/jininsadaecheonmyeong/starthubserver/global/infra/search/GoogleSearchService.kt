package com.jininsadaecheonmyeong.starthubserver.global.infra.search

import com.jininsadaecheonmyeong.starthubserver.global.config.GoogleSearchProperties
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.CompetitorSearchResult
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.GoogleSearchResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Service
class GoogleSearchService(
    @Qualifier("googleSearchWebClient")
    private val webClient: WebClient,
    private val properties: GoogleSearchProperties,
) {
    private val logger = LoggerFactory.getLogger(GoogleSearchService::class.java)

    suspend fun searchCompany(name: String): CompetitorSearchResult? {
        return try {
            val response =
                webClient.get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .queryParam("key", properties.apiKey)
                            .queryParam("cx", properties.searchEngineId)
                            .queryParam("q", "$name 공식 사이트")
                            .queryParam("num", 3)
                            .build()
                    }
                    .retrieve()
                    .bodyToMono(GoogleSearchResponse::class.java)
                    .timeout(Duration.ofMillis(properties.timeout))
                    .awaitSingle()

            val item = response.items?.firstOrNull() ?: return null

            CompetitorSearchResult(
                title = name,
                url = item.link,
                snippet = item.snippet,
                displayUrl = item.displayLink,
            )
        } catch (e: WebClientResponseException) {
            logger.warn(
                "Google Search 실패 - 회사명: {}, 상태: {}, 응답: {}",
                name,
                e.statusCode,
                e.responseBodyAsString,
            )
            null
        } catch (e: Exception) {
            logger.warn("Google Search 실패 - 회사명: {}, 오류: {}", name, e.message)
            null
        }
    }

    suspend fun searchCompanies(names: List<String>): List<CompetitorSearchResult> =
        coroutineScope {
            names.map { name ->
                async { searchCompany(name) }
            }.awaitAll().filterNotNull()
        }
}
