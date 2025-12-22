package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SearchAnnouncementsUseCase {
    fun searchAnnouncements(query: SearchAnnouncementsQuery, pageable: Pageable): Page<AnnouncementQueryResult>
}

data class SearchAnnouncementsQuery(
    val title: String? = null,
    val supportField: String? = null,
    val targetRegion: String? = null,
    val targetGroup: String? = null,
    val targetAge: String? = null,
    val includeLikeStatus: Boolean = false
)
