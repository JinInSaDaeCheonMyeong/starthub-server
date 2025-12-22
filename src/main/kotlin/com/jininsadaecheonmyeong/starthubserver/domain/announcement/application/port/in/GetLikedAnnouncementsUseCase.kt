package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetLikedAnnouncementsUseCase {
    fun getLikedAnnouncements(pageable: Pageable): Page<AnnouncementQueryResult>
}
