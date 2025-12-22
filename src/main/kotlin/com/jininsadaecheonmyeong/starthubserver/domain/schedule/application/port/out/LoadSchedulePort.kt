package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import java.time.LocalDate

interface LoadSchedulePort {
    fun loadByMonth(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Schedule>
    fun loadByDate(userId: Long, date: LocalDate): List<Schedule>
}
