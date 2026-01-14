package com.jininsadaecheonmyeong.starthubserver.dto.request.recruit

data class CreateRecruitRequest(
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
)
