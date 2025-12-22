package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web.request

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.CreateScheduleCommand
import java.time.LocalDate

data class ScheduleWebRequest(
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun toCommand(userId: Long) = CreateScheduleCommand(
        userId = userId,
        announcementId = announcementId,
        startDate = startDate,
        endDate = endDate
    )
}
