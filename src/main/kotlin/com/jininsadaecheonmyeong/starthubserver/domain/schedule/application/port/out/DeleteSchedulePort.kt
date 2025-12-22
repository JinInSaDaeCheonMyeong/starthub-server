package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out

interface DeleteSchedulePort {
    fun deleteByAnnouncementIdAndUserId(announcementId: Long, userId: Long)
}
