package com.jininsadaecheonmyeong.starthubserver.global.infra.search

import com.jininsadaecheonmyeong.starthubserver.global.infra.search.exception.CompanyInfoExtractException
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.CompanyInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.search.model.CompetitorSearchResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import java.time.Duration

@Service
class CompanyInfoService(
    @Qualifier("companyInfoWebClient")
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(CompanyInfoService::class.java)

    fun extractCompanyInfo(searchResult: CompetitorSearchResult): CompanyInfo? {
        return try {
            val htmlContent = fetchWebsiteContent(searchResult.url)
            val document = Jsoup.parse(htmlContent)

            val companyInfo = buildCompanyInfo(document, searchResult)

            logger.debug("Successfully extracted company info for: {}", searchResult.domain)
            companyInfo
        } catch (e: Exception) {
            logger.warn("Failed to extract company info from {}: {}", searchResult.url, e.message)
            createFallbackCompanyInfo(searchResult)
        }
    }

    fun extractCompanyLogo(websiteUrl: String): String? {
        return try {
            val htmlContent = fetchWebsiteContent(websiteUrl)
            val document = Jsoup.parse(htmlContent)

            extractLogoFromDocument(document, websiteUrl)
                ?.also { logger.debug("Logo found for {}: {}", websiteUrl, it) }
        } catch (e: Exception) {
            logger.warn("Failed to extract logo from {}: {}", websiteUrl, e.message)
            null
        }
    }

    private fun fetchWebsiteContent(url: String): String {
        return webClient.get()
            .uri(url)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
            .header("Accept-Encoding", "gzip, deflate")
            .header("DNT", "1")
            .header("Connection", "keep-alive")
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofSeconds(10))
            .retryWhen(
                Retry.backoff(2, Duration.ofSeconds(1))
                    .filter { it !is WebClientResponseException.NotFound },
            )
            .onErrorMap { ex ->
                CompanyInfoExtractException("Failed to fetch content from $url", ex)
            }
            .block() ?: throw CompanyInfoExtractException("No content received from $url")
    }

    private fun buildCompanyInfo(
        document: Document,
        searchResult: CompetitorSearchResult,
    ): CompanyInfo {
        return CompanyInfo(
            name = extractCompanyName(document, searchResult),
            description = extractDescription(document, searchResult),
            logoUrl = extractLogoFromDocument(document, searchResult.url),
            websiteUrl = searchResult.url,
            domain = searchResult.domain,
            industry = extractIndustry(document),
            foundedYear = extractFoundedYear(document),
            headquarters = extractHeadquarters(document),
            socialLinks = extractSocialLinks(document),
        )
    }

    private fun extractCompanyName(
        document: Document,
        searchResult: CompetitorSearchResult,
    ): String {
        val candidates =
            listOfNotNull(
                document.selectFirst("meta[property=og:site_name]")?.attr("content"),
                document.selectFirst("meta[name=application-name]")?.attr("content"),
                document.selectFirst("h1")?.text(),
                document.selectFirst(".company-name, .brand-name, .logo-text")?.text(),
                document.title().split(" - ", " | ", " · ").firstOrNull(),
                searchResult.title.split(" - ", " | ", " · ").firstOrNull(),
            )

        return candidates.firstOrNull { it.isNotBlank() && it.length > 1 }
            ?.trim()
            ?.take(100)
            ?: searchResult.domain.split(".").first().replaceFirstChar { it.uppercase() }
    }

    private fun extractDescription(
        document: Document,
        searchResult: CompetitorSearchResult,
    ): String {
        val candidates =
            listOfNotNull(
                document.selectFirst("meta[name=description]")?.attr("content"),
                document.selectFirst("meta[property=og:description]")?.attr("content"),
                document.selectFirst("meta[name=twitter:description]")?.attr("content"),
                document.selectFirst(".company-description, .about-us, .description")?.text(),
                searchResult.snippet,
            )

        return candidates.firstOrNull { it.isNotBlank() && it.length > 10 }
            ?.trim()
            ?.take(500)
            ?: "No description available"
    }

    private fun extractLogoFromDocument(
        document: Document,
        baseUrl: String,
    ): String? {
        val logoSelectors =
            listOf(
                "link[rel*=icon]",
                "meta[property=og:image]",
                "img[alt*=logo i], img[class*=logo i], img[src*=logo i]",
                ".logo img, .brand img, .header-logo img",
                "img[width='200'], img[height='100']",
            )

        for (selector in logoSelectors) {
            val elements = document.select(selector)
            for (element in elements) {
                val logoUrl =
                    when (element.tagName()) {
                        "link" -> element.attr("href")
                        "meta" -> element.attr("content")
                        "img" -> element.attr("src")
                        else -> continue
                    }

                if (logoUrl.isNotBlank() && isValidLogoUrl(logoUrl)) {
                    return resolveUrl(logoUrl, baseUrl)
                }
            }
        }

        return null
    }

    private fun extractIndustry(document: Document): String? {
        val industrySelectors =
            listOf(
                "meta[name=industry]",
                "meta[property=business:category]",
                ".industry, .business-category, .sector",
            )

        return industrySelectors.asSequence()
            .mapNotNull { selector ->
                val element = document.selectFirst(selector)
                when (element?.tagName()) {
                    "meta" -> element.attr("content")
                    else -> element?.text()
                }
            }
            .firstOrNull { it.isNotBlank() }
            ?.take(100)
    }

    private fun extractFoundedYear(document: Document): String? {
        val text = document.text()
        val yearPattern = Regex("(?:설립|창립|founded|established|since)\\s*:?\\s*(\\d{4})")

        return yearPattern.find(text)
            ?.groupValues
            ?.get(1)
            ?.let { year ->
                val yearInt = year.toIntOrNull()
                if (yearInt != null && yearInt in 1800..2024) year else null
            }
    }

    private fun extractHeadquarters(document: Document): String? {
        val locationSelectors =
            listOf(
                "meta[name=geo.placename]",
                "meta[name=location]",
                ".headquarters, .location, .address",
            )

        return locationSelectors.asSequence()
            .mapNotNull { selector ->
                val element = document.selectFirst(selector)
                when (element?.tagName()) {
                    "meta" -> element.attr("content")
                    else -> element?.text()
                }
            }
            .firstOrNull { it.isNotBlank() && it.length > 3 }
            ?.take(200)
    }

    private fun extractSocialLinks(document: Document): CompanyInfo.SocialLinks {
        fun findSocialLink(platform: String): String? {
            return document.select("a[href*=$platform]")
                .map { it.attr("href") }
                .firstOrNull { it.contains(platform, ignoreCase = true) }
        }

        return CompanyInfo.SocialLinks(
            linkedin = findSocialLink("linkedin.com"),
            twitter = findSocialLink("twitter.com") ?: findSocialLink("x.com"),
            facebook = findSocialLink("facebook.com"),
            instagram = findSocialLink("instagram.com"),
        )
    }

    private fun createFallbackCompanyInfo(searchResult: CompetitorSearchResult): CompanyInfo {
        return CompanyInfo(
            name = searchResult.domain.split(".").first().replaceFirstChar { it.uppercase() },
            description = searchResult.snippet.take(200),
            logoUrl = null,
            websiteUrl = searchResult.url,
            domain = searchResult.domain,
        )
    }

    private fun isValidLogoUrl(url: String): Boolean {
        val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp")
        val lowercaseUrl = url.lowercase()

        return imageExtensions.any { lowercaseUrl.contains(it) } ||
            url.contains("logo", ignoreCase = true) ||
            url.contains("brand", ignoreCase = true)
    }

    private fun resolveUrl(
        url: String,
        baseUrl: String,
    ): String {
        return when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> {
                val base = baseUrl.substringBefore("/", baseUrl.substringAfter("://"))
                "https://$base$url"
            }
            else -> {
                val base = baseUrl.substringBeforeLast("/")
                "$base/$url"
            }
        }
    }
}
