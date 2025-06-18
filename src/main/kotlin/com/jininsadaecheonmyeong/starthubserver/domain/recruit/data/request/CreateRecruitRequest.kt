package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.enums.RecruitRole

data class CreateRecruitRequest(
    val role: RecruitRole,
    val count: Int,
    val description: String
)