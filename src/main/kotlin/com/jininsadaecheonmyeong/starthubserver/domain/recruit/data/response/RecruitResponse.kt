
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response

import java.time.LocalDateTime

data class RecruitResponse(
    val id: Long,
    val title: String,
    val content: String,
    val writerId: Long,
    val writerNickname: String,
    val companyId: Long,
    val companyName: String,
    val startDate: String,
    val endDate: String,
    val desiredCareer: String,
    val workType: String,
    val jobType: String,
    val requiredPeople: Int,
    val viewCount: Int,
    val isClosed: Boolean,
    val techStack: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
