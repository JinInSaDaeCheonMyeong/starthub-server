package com.jininsadaecheonmyeong.starthubserver.dto.request.schedule

import java.time.LocalDate

data class ScheduleRequest(
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
