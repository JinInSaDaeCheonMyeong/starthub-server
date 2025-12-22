package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike

interface DeleteAnnouncementLikePort {
    fun delete(announcementLike: AnnouncementLike)
}
