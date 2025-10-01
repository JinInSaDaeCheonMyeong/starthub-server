package com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.request

import java.time.LocalDate

data class ScheduleRequest(
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate
)
