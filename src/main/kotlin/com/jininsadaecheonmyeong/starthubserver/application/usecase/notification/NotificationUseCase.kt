package com.jininsadaecheonmyeong.starthubserver.application.usecase.notification

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.notification.DeviceType
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.notification.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.notification.NotificationHistoryResponse

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
