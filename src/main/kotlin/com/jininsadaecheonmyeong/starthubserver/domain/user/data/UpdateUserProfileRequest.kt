package com.jininsadaecheonmyeong.starthubserver.domain.user.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

data class UpdateUserProfileRequest(
    val username: String,
    val interests: List<BusinessType>,
    val profileImage: String
)
