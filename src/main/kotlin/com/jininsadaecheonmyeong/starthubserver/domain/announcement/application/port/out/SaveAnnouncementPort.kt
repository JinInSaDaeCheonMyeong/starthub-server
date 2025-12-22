package com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement

interface SaveAnnouncementPort {
    fun save(announcement: Announcement): Announcement
}
