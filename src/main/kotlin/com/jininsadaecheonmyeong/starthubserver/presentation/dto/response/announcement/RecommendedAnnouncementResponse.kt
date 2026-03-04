package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement

import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement

data class RecommendedAnnouncementResponse(
    val id: Long,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val likeCount: Int,
    val supportField: String,
    val targetAge: String,
    val region: String,
    val organizationType: String,
    val startupHistory: String,
    val content: String,
    val isLiked: Boolean,
    val score: Double? = null,
) {
    companion object {
        fun from(
            announcement: Announcement,
            score: Double? = null,
            isLiked: Boolean,
        ) = RecommendedAnnouncementResponse(
            id = announcement.id!!,
            title = announcement.title,
            url = announcement.url,
            organization = announcement.organization,
            receptionPeriod = announcement.receptionPeriod,
            likeCount = announcement.likeCount,
            supportField = announcement.supportField,
            targetAge = announcement.targetAge,
            region = announcement.region,
            organizationType = announcement.organizationType,
            startupHistory = announcement.startupHistory,
            content = announcement.content,
            isLiked = isLiked,
            score = score,
        )
    }
}
