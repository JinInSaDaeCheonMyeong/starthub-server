package com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.notification.enums.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterFcmTokenRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val token: String,
    @field:NotNull(message = "디바이스 타입은 필수입니다.")
    val deviceType: DeviceType = DeviceType.UNKNOWN,
)
