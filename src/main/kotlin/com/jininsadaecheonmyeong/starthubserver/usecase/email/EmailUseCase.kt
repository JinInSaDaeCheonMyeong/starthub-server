package com.jininsadaecheonmyeong.starthubserver.usecase.email

interface EmailUseCase {
    fun sendVerificationCode(email: String)

    fun verifyCode(
        email: String,
        code: String,
    )
}
