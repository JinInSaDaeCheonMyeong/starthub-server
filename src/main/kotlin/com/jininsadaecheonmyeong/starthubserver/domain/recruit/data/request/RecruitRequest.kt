package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

data class RecruitRequest(
    val title: String,
    val content: String,
    val companyId: Long,
    val startDate: String,
    val endDate: String,
    val desiredCareer: String,
    val workType: String,
    val jobType: String,
    val requiredPeople: Int,
    val techStack: List<String>,
    val businessType: BusinessType,
    val tags: List<String>?,
)
