package com.jininsadaecheonmyeong.starthubserver.global.infra.search

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class CompanyInfoService(
    @Qualifier("companyInfoWebClient")
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(CompanyInfoService::class.java)

    fun getLogoUrl(domain: String): String {
        val clearbitUrl = "https://logo.clearbit.com/$domain"

        return try {
            val response =
                webClient.method(HttpMethod.HEAD)
                    .uri(clearbitUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(5))
                    .block()

            if (response?.statusCode?.is2xxSuccessful == true) {
                clearbitUrl
            } else {
                getFaviconFallback(domain)
            }
        } catch (e: Exception) {
            logger.debug("Clearbit 로고 조회 실패 - domain: {}, fallback 사용", domain)
            getFaviconFallback(domain)
        }
    }

    private fun getFaviconFallback(domain: String): String {
        return "https://www.google.com/s2/favicons?domain=$domain&sz=128"
    }
}
