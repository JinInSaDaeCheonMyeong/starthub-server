package com.jininsadaecheonmyeong.starthubserver.usecase.notification

import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.NotificationHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.notification.DeviceType

interface NotificationUseCase {
    fun registerToken(
        user: User,
        token: String,
        deviceType: DeviceType = DeviceType.UNKNOWN,
    )

    fun deleteToken(token: String)

    fun sendPushNotificationToUser(
        user: User,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    )

    fun getTokensByUser(user: User): List<FcmTokenResponse>

    fun getNotificationHistory(user: User): List<NotificationHistoryResponse>
}
