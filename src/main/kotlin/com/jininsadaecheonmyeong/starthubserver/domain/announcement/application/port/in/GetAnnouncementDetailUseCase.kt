package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`

interface GetAnnouncementDetailUseCase {
    fun getAnnouncementDetail(announcementId: Long, includeLikeStatus: Boolean): AnnouncementQueryResult
}
