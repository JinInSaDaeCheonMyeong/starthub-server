package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import java.time.LocalDate

interface CreateScheduleUseCase {
    fun createSchedule(command: CreateScheduleCommand): Schedule
}

data class CreateScheduleCommand(
    val userId: Long,
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate
)
