package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.AnnouncementQueryResult
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetAnnouncementDetailUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetAnnouncementDetailService(
    private val loadAnnouncementPort: LoadAnnouncementPort,
    private val loadAnnouncementLikePort: LoadAnnouncementLikePort,
    private val getCurrentUserPort: GetCurrentUserPort
) : GetAnnouncementDetailUseCase {

    override fun getAnnouncementDetail(announcementId: Long, includeLikeStatus: Boolean): AnnouncementQueryResult {
        val announcement = loadAnnouncementPort.loadById(announcementId)
            ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        val isLiked = if (includeLikeStatus) {
            val userId = getCurrentUserPort.getCurrentUserId()
            loadAnnouncementLikePort.existsByUserIdAndAnnouncementId(userId, announcementId)
        } else {
            false
        }

        return AnnouncementQueryResult(announcement, isLiked)
    }
}
