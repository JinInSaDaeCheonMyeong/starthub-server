package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import java.time.LocalDate

interface GetScheduleUseCase {
    fun getSchedulesByMonth(userId: Long, date: LocalDate): List<Schedule>
    fun getSchedulesByDate(userId: Long, date: LocalDate): List<Schedule>
}
