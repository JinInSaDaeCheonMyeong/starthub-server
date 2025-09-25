package com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendationRequest(
    val interests: List<String>,
    @get:JsonProperty("liked_announcements")
    val likedAnnouncements: LikedAnnouncementsContent,
)

data class LikedAnnouncementsContent(
    val content: List<LikedAnnouncementUrl>,
)

data class LikedAnnouncementUrl(
    val url: String,
)
