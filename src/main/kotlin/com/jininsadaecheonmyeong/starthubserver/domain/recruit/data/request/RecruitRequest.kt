package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request

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
    val tags: List<String>?,
)
