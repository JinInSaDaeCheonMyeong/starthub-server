package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.recruit

import java.time.LocalDateTime

data class RecruitPreviewResponse(
    val id: Long,
    val title: String,
    val companyName: String,
    val endDate: String,
    val viewCount: Int,
    val isClosed: Boolean,
    val createdAt: LocalDateTime,
)
