package com.jininsadaecheonmyeong.starthubserver.global.infra.search.model

data class SearchRequest(
    val query: String,
    val maxResults: Int = 10,
    val startIndex: Int = 1,
    val siteSearch: String? = null,
    val excludeTerms: List<String> = emptyList(),
    val bmcContext: BmcContext? = null,
) {
    val formattedQuery: String
        get() =
            buildString {
                append(query)
                if (excludeTerms.isNotEmpty()) {
                    append(" ")
                    append(excludeTerms.joinToString(" ") { "-$it" })
                }
            }
}

data class BmcContext(
    val title: String,
    val valueProposition: String?,
    val customerSegments: String?,
    val channels: String?,
    val revenueStreams: String?,
)

data class CompetitorSearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val displayUrl: String = "",
    val thumbnailUrl: String? = null,
    val relevanceScore: Double = 0.0,
) {
    val domain: String
        get() =
            try {
                url.substringAfter("://").substringBefore("/").removePrefix("www.")
            } catch (e: Exception) {
                ""
            }

    val isValidBusinessSite: Boolean
        get() =
            domain.isNotBlank() &&
                !domain.contains("google.") &&
                !domain.contains("youtube.") &&
                !domain.contains("facebook.") &&
                !domain.contains("twitter.") &&
                !domain.contains("linkedin.") &&
                !domain.contains("wikipedia.")
}

data class CompanyInfo(
    val name: String,
    val description: String,
    val logoUrl: String? = null,
    val websiteUrl: String,
    val domain: String = "",
    val industry: String? = null,
    val foundedYear: String? = null,
    val headquarters: String? = null,
    val socialLinks: SocialLinks = SocialLinks(),
) {
    data class SocialLinks(
        val linkedin: String? = null,
        val twitter: String? = null,
        val facebook: String? = null,
        val instagram: String? = null,
    )
}

enum class SearchResultType {
    BUSINESS_WEBSITE,
    COMPANY_PROFILE,
    PRODUCT_PAGE,
    NEWS_ARTICLE,
    SOCIAL_MEDIA,
    OTHER,
}

data class EnhancedSearchResult(
    val basicResult: CompetitorSearchResult,
    val type: SearchResultType,
    val businessRelevance: Double,
    val companyInfo: CompanyInfo? = null,
)
