package com.jininsadaecheonmyeong.starthubserver.dto.response.notification

import com.jininsadaecheonmyeong.starthubserver.entity.notification.FcmToken
import com.jininsadaecheonmyeong.starthubserver.enums.notification.DeviceType
import java.time.LocalDateTime

data class FcmTokenResponse(
    val id: Long,
    val token: String,
    val deviceType: DeviceType,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(fcmToken: FcmToken): FcmTokenResponse {
            return FcmTokenResponse(
                id = fcmToken.id!!,
                token = fcmToken.token,
                deviceType = fcmToken.deviceType,
                createdAt = fcmToken.createdAt,
            )
        }
    }
}
