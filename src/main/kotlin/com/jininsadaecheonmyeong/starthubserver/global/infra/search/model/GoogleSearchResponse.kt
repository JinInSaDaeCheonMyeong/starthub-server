package com.jininsadaecheonmyeong.starthubserver.global.infra.search.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleSearchResponse(
    val items: List<SearchItem>? = emptyList(),
    val searchInformation: SearchInformation? = null,
    val queries: SearchQueries? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchItem(
    val title: String = "",
    val link: String = "",
    val snippet: String = "",
    val displayLink: String = "",
    @JsonProperty("formattedUrl")
    val formattedUrl: String = "",
    val pagemap: PageMap? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchInformation(
    val totalResults: String = "0",
    val searchTime: Double = 0.0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchQueries(
    val request: List<QueryInfo>? = emptyList(),
    val nextPage: List<QueryInfo>? = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueryInfo(
    val title: String = "",
    val totalResults: String = "0",
    val searchTerms: String = "",
    val count: Int = 0,
    val startIndex: Int = 1,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageMap(
    val cse_thumbnail: List<ThumbnailInfo>? = emptyList(),
    val metatags: List<MetaTag>? = emptyList(),
    val organization: List<OrganizationInfo>? = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ThumbnailInfo(
    val src: String = "",
    val width: String = "",
    val height: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaTag(
    @JsonProperty("og:title")
    val ogTitle: String? = null,
    @JsonProperty("og:description")
    val ogDescription: String? = null,
    @JsonProperty("og:image")
    val ogImage: String? = null,
    @JsonProperty("og:url")
    val ogUrl: String? = null,
    @JsonProperty("og:site_name")
    val ogSiteName: String? = null,
    val description: String? = null,
    val keywords: String? = null,
    val author: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationInfo(
    val name: String? = null,
    val logo: String? = null,
    val url: String? = null,
)
