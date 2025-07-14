
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response

import java.time.LocalDateTime

data class RecruitSummaryResponse(
    val id: Long,
    val title: String,
    val companyName: String,
    val endDate: String,
    val viewCount: Int,
    val isClosed: Boolean,
    val createdAt: LocalDateTime,
)
