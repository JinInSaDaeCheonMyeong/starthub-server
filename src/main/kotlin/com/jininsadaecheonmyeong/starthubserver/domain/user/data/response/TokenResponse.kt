package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import java.time.LocalDateTime

data class TokenResponse(
    val access: String,
    val refresh: String,
    val isFirstLogin: Boolean,
    val isAccountRestored: Boolean = false,
    val deletedAt: LocalDateTime? = null,
)
