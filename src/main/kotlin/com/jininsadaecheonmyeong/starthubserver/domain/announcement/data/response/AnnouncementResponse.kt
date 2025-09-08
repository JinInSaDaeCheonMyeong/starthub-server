package com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement

data class AnnouncementResponse(
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val likeCount: Int,
    val isLiked: Boolean,
) {
    companion object {
        fun from(
            announcement: Announcement,
            isLiked: Boolean,
        ) = AnnouncementResponse(
            title = announcement.title,
            url = announcement.url,
            organization = announcement.organization,
            receptionPeriod = announcement.receptionPeriod,
            likeCount = announcement.likeCount,
            isLiked = isLiked,
        )
    }
}
