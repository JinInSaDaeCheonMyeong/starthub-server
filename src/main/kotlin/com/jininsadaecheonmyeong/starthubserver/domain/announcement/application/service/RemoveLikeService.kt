package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.RemoveLikeUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.DeleteAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.SaveAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RemoveLikeService(
    private val loadAnnouncementPort: LoadAnnouncementPort,
    private val saveAnnouncementPort: SaveAnnouncementPort,
    private val loadAnnouncementLikePort: LoadAnnouncementLikePort,
    private val deleteAnnouncementLikePort: DeleteAnnouncementLikePort,
    private val getCurrentUserPort: GetCurrentUserPort
) : RemoveLikeUseCase {

    override fun removeLike(announcementId: Long) {
        val userId = getCurrentUserPort.getCurrentUserId()

        val announcement = loadAnnouncementPort.loadById(announcementId)
            ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        val like = loadAnnouncementLikePort.loadByUserIdAndAnnouncementId(userId, announcementId)
            ?: throw LikeNotFoundException("좋아요를 누르지 않은 공고")

        deleteAnnouncementLikePort.delete(like)

        val updatedAnnouncement = announcement.decrementLikeCount()
        saveAnnouncementPort.save(updatedAnnouncement)
    }
}
