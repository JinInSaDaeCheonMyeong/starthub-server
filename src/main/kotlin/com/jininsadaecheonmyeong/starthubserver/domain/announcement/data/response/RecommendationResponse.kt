package com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendationResponse(
    @get:JsonProperty("recommendations")
    val recommendations: List<RecommendationItem>,
)

data class RecommendationItem(
    @get:JsonProperty("title")
    val title: String,
    @get:JsonProperty("url")
    val url: String,
    @get:JsonProperty("score")
    val score: Double,
)
