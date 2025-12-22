package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web.response

import java.time.LocalDate

data class ScheduleWebResponse(
    val announcementId: Long,
    val supportField: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
