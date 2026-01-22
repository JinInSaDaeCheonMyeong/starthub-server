package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.email

import jakarta.validation.constraints.Email

data class EmailVerifyRequest(
    @field:Email val email: String,
    val code: String,
)
