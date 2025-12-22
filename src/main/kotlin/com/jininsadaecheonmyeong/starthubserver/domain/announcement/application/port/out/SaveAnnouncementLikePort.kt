package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike

interface SaveAnnouncementLikePort {
    fun save(announcementLike: AnnouncementLike): AnnouncementLike
}
