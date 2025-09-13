package com.jininsadaecheonmyeong.starthubserver.domain.recommendation.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class RecommendationResponse(
    val userId: Long,
    val recommendations: List<RecommendedAnnouncementResponse>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val generatedAt: LocalDateTime,
    val filtersApplied: RecommendationFilters? = null
)

data class RecommendedAnnouncementResponse(
    val announcementId: Long,
    val title: String,
    val organization: String,
    val supportField: String? = null,
    val targetAge: String? = null,
    val region: String? = null,
    val similarityScore: Double,
    val metadata: Map<String, Any>? = null
)

data class RecommendationFilters(
    val activeOnly: Boolean,
    val region: String? = null,
    val supportField: String? = null
)

// FastAPI 응답을 매핑하기 위한 내부 DTO
data class FastApiRecommendationResponse(
    @JsonProperty("user_id") val userId: Long,
    val recommendations: List<FastApiRecommendedAnnouncement>,
    @JsonProperty("total_count") val totalCount: Int,
    val page: Int,
    @JsonProperty("page_size") val pageSize: Int,
    @JsonProperty("total_pages") val totalPages: Int,
    @JsonProperty("has_next") val hasNext: Boolean,
    @JsonProperty("has_previous") val hasPrevious: Boolean,
    @JsonProperty("generated_at") val generatedAt: String,
    @JsonProperty("filters_applied") val filtersApplied: FastApiFilters? = null
)

data class FastApiRecommendedAnnouncement(
    @JsonProperty("announcement_id") val announcementId: Long,
    val title: String,
    val organization: String,
    @JsonProperty("support_field") val supportField: String? = null,
    @JsonProperty("target_age") val targetAge: String? = null,
    val region: String? = null,
    @JsonProperty("similarity_score") val similarityScore: Double,
    val metadata: Map<String, Any>? = null
)

data class FastApiFilters(
    @JsonProperty("active_only") val activeOnly: Boolean,
    val region: String? = null,
    @JsonProperty("support_field") val supportField: String? = null
)