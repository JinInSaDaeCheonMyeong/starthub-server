package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.AnnouncementQueryResult
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetAnnouncementsUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetAnnouncementsService(
    private val loadAnnouncementPort: LoadAnnouncementPort,
    private val loadAnnouncementLikePort: LoadAnnouncementLikePort,
    private val getCurrentUserPort: GetCurrentUserPort
) : GetAnnouncementsUseCase {

    override fun getAnnouncements(pageable: Pageable, includeLikeStatus: Boolean): Page<AnnouncementQueryResult> {
        val announcements = loadAnnouncementPort.loadAllByStatus(AnnouncementStatus.ACTIVE, pageable)

        return if (includeLikeStatus) {
            val userId = getCurrentUserPort.getCurrentUserId()
            announcements.map { announcement ->
                val isLiked = loadAnnouncementLikePort.existsByUserIdAndAnnouncementId(
                    userId,
                    announcement.id!!
                )
                AnnouncementQueryResult(announcement, isLiked)
            }
        } else {
            announcements.map { AnnouncementQueryResult(it, false) }
        }
    }
}
