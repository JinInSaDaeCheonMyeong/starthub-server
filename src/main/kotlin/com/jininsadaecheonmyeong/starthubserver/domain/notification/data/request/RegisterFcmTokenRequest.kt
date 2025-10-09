package com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request

import jakarta.validation.constraints.NotBlank

data class RegisterFcmTokenRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val token: String,
    val deviceType: String = "UNKNOWN",
)
