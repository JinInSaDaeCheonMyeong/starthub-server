package com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnouncementRepository : JpaRepository<Announcement, Long> {
    fun existsByUrl(url: String): Boolean

    fun findAllByStatus(status: AnnouncementStatus, pageable: Pageable): Page<Announcement>

    fun findAllByStatus(status: AnnouncementStatus): List<Announcement>
}
