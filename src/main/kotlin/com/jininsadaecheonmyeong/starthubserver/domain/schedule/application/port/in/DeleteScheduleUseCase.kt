package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`

interface DeleteScheduleUseCase {
    fun deleteSchedule(userId: Long, announcementId: Long)
}
