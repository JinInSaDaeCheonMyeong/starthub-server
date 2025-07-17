package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

data class UserProfileResponse(
    val username: String?,
    val profileImage: String?,
    val companyIds: List<Long>,
)
