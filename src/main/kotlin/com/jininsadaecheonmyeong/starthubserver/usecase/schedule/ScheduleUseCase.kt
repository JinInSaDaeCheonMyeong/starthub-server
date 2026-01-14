package com.jininsadaecheonmyeong.starthubserver.usecase.schedule

import com.jininsadaecheonmyeong.starthubserver.dto.request.schedule.ScheduleRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.schedule.DailyScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.schedule.ScheduleResponse
import java.time.LocalDate

interface ScheduleUseCase {
    fun createSchedule(
        userId: Long,
        request: ScheduleRequest,
    )

    fun deleteSchedule(
        userId: Long,
        announcementId: Long,
    )

    fun getSchedulesByMonth(
        userId: Long,
        date: LocalDate,
    ): List<ScheduleResponse>

    fun getSchedulesByDate(
        userId: Long,
        date: LocalDate,
    ): List<DailyScheduleResponse>
}
