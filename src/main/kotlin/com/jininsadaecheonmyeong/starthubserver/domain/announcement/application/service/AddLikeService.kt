package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.AddLikeUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.DeleteAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.SaveAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.SaveAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AddLikeService(
    private val loadAnnouncementPort: LoadAnnouncementPort,
    private val saveAnnouncementPort: SaveAnnouncementPort,
    private val loadAnnouncementLikePort: LoadAnnouncementLikePort,
    private val saveAnnouncementLikePort: SaveAnnouncementLikePort,
    private val getCurrentUserPort: GetCurrentUserPort
) : AddLikeUseCase {

    override fun addLike(announcementId: Long) {
        val userId = getCurrentUserPort.getCurrentUserId()

        val announcement = loadAnnouncementPort.loadById(announcementId)
            ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        if (loadAnnouncementLikePort.existsByUserIdAndAnnouncementId(userId, announcementId)) {
            throw LikeAlreadyExistsException("이미 좋아요를 누른 공고")
        }

        val like = AnnouncementLike.create(userId, announcementId)
        saveAnnouncementLikePort.save(like)

        val updatedAnnouncement = announcement.incrementLikeCount()
        saveAnnouncementPort.save(updatedAnnouncement)
    }
}
