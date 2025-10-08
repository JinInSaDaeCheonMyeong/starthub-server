package com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response

import java.time.LocalDate

data class ScheduleResponse(
    val announcementId: Long,
    val supportField: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
