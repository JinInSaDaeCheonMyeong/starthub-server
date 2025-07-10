package com.jininsadaecheonmyeong.starthubserver.domain.user.data

data class TokenResponse(
    val access: String,
    val refresh: String,
    val isFirstLogin: Boolean
)