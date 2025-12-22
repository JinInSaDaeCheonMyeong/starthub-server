package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadAnnouncementLikePort {
    fun existsByUserIdAndAnnouncementId(userId: Long, announcementId: Long): Boolean
    fun loadByUserIdAndAnnouncementId(userId: Long, announcementId: Long): AnnouncementLike?
    fun loadByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<AnnouncementLike>
}
