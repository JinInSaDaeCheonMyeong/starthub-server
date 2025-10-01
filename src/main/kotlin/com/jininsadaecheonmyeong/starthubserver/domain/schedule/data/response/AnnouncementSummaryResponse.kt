package com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response

data class AnnouncementSummaryResponse(
    val id: Long,
    val title: String,
    val organization: String,
    val receptionPeriod: String,
    val likeCount: Int,
)
