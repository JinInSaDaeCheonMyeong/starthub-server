package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadAnnouncementPort {
    fun loadById(id: Long): Announcement?
    fun loadAllByStatus(status: AnnouncementStatus, pageable: Pageable): Page<Announcement>
    fun loadAllByStatus(status: AnnouncementStatus): List<Announcement>
    fun existsByUrl(url: String): Boolean
}
