package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendationRequest(
    val interests: List<String>,
    @get:JsonProperty("liked_announcements")
    val likedAnnouncements: LikedAnnouncementsContent,
    @get:JsonProperty("bmcs")
    val bmcs: List<BmcInfo>,
)

data class LikedAnnouncementsContent(
    val content: List<LikedAnnouncementUrl>,
)

data class LikedAnnouncementUrl(
    val url: String,
)

data class BmcInfo(
    val customerSegments: String?,
    val valueProposition: String?,
    val channels: String?,
    val customerRelationships: String?,
    val revenueStreams: String?,
    val keyResources: String?,
    val keyActivities: String?,
    val keyPartners: String?,
    val costStructure: String?,
)
