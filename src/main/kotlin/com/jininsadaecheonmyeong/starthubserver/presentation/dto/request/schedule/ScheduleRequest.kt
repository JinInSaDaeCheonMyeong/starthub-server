package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.schedule

import java.time.LocalDate

data class ScheduleRequest(
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
