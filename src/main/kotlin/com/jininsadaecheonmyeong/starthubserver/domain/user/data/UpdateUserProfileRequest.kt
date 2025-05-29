package com.jininsadaecheonmyeong.starthubserver.domain.user.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.InterestType

data class UpdateUserProfileRequest(
    val username: String,
    val interests: List<InterestType>
)
