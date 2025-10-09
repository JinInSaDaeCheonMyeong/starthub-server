package com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.notification.entity.FcmToken
import java.time.LocalDateTime

data class FcmTokenResponse(
    val id: Long,
    val token: String,
    val deviceType: String,
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
