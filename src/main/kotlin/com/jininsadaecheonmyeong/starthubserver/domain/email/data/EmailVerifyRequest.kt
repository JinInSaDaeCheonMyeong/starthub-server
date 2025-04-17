package com.jininsadaecheonmyeong.starthubserver.domain.email.data

data class EmailVerifyRequest (
    val email: String,
    val code: String
)