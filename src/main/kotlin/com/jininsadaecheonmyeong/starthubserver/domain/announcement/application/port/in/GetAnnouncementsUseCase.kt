package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetAnnouncementsUseCase {
    fun getAnnouncements(pageable: Pageable, includeLikeStatus: Boolean): Page<AnnouncementQueryResult>
}

data class AnnouncementQueryResult(
    val announcement: Announcement,
    val isLiked: Boolean = false
)
