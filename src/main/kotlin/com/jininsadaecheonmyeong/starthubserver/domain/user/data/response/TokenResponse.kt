package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

data class TokenResponse(
    val access: String,
    val refresh: String,
    val isFirstLogin: Boolean,
)
