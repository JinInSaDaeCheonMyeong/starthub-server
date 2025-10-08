package com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response

data class DailyScheduleResponse(
    val id: Long,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val likeCount: Int,
    val supportField: String,
    val targetAge: String,
    val contactNumber: String,
    val region: String,
    val organizationType: String,
    val startupHistory: String,
    val departmentInCharge: String,
    val content: String,
    val isLiked: Boolean,
)
