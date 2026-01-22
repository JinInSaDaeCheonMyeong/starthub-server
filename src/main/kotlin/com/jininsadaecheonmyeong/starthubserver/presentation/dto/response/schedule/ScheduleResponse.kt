package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.schedule

import java.time.LocalDate

data class ScheduleResponse(
    val announcementId: Long,
    val supportField: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
