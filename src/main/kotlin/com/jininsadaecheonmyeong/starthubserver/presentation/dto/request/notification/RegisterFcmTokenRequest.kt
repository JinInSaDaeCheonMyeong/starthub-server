package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.notification

import com.jininsadaecheonmyeong.starthubserver.domain.enums.notification.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterFcmTokenRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val token: String,
    @field:NotNull(message = "디바이스 타입은 필수입니다.")
    var deviceType: DeviceType = DeviceType.UNKNOWN,
)
