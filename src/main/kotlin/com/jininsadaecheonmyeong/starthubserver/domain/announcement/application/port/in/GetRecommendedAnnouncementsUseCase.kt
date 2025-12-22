package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement

interface GetRecommendedAnnouncementsUseCase {
    fun getRecommendedAnnouncements(): List<RecommendedAnnouncementResult>
}

data class RecommendedAnnouncementResult(
    val announcement: Announcement,
    val reason: String
)
