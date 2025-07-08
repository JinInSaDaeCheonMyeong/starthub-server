package com.jininsadaecheonmyeong.starthubserver.domain.email.data

import jakarta.validation.constraints.Email

data class EmailVerifyRequest(
    @field:Email val email: String,
    val code: String,
)
