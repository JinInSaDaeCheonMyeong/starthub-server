package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.AnnouncementQueryResult
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetLikedAnnouncementsUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetLikedAnnouncementsService(
    private val loadAnnouncementLikePort: LoadAnnouncementLikePort,
    private val loadAnnouncementPort: LoadAnnouncementPort,
    private val getCurrentUserPort: GetCurrentUserPort
) : GetLikedAnnouncementsUseCase {

    override fun getLikedAnnouncements(pageable: Pageable): Page<AnnouncementQueryResult> {
        val userId = getCurrentUserPort.getCurrentUserId()
        val likedAnnouncements = loadAnnouncementLikePort.loadByUserIdOrderByCreatedAtDesc(userId, pageable)

        return likedAnnouncements.map { like ->
            val announcement = loadAnnouncementPort.loadById(like.announcementId)
                ?: throw IllegalStateException("Announcement not found for like: ${like.id}")
            AnnouncementQueryResult(announcement, true)
        }
    }
}
