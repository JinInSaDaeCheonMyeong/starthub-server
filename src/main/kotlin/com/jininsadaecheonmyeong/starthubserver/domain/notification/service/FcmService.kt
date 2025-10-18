package com.jininsadaecheonmyeong.starthubserver.domain.notification.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response.NotificationHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.domain.notification.entity.FcmToken
import com.jininsadaecheonmyeong.starthubserver.domain.notification.enums.DeviceType
import com.jininsadaecheonmyeong.starthubserver.domain.notification.repository.FcmTokenRepository
import com.jininsadaecheonmyeong.starthubserver.domain.notification.repository.NotificationHistoryRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FcmService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
) {
    private val logger = LoggerFactory.getLogger(FcmService::class.java)

    @Transactional
    fun registerToken(
        user: User,
        token: String,
        deviceType: DeviceType = DeviceType.UNKNOWN,
    ) {
        val existingToken = fcmTokenRepository.findByUserAndDeviceType(user, deviceType)
        if (existingToken.isPresent) {
            val fcmToken = existingToken.get()
            if (fcmToken.token != token) {
                fcmToken.updateToken(token)
                fcmTokenRepository.save(fcmToken)
                logger.info("FCM token updated for user: ${user.id}, deviceType: $deviceType")
            } else {
                logger.info("FCM token already exists for user: ${user.id}, deviceType: $deviceType")
            }
            return
        }

        val fcmToken = FcmToken(user = user, token = token, deviceType = deviceType)
        fcmTokenRepository.save(fcmToken)
        logger.info("FCM token registered for user: ${user.id}, deviceType: $deviceType")
    }

    @Transactional
    fun deleteToken(token: String) {
        fcmTokenRepository.deleteByToken(token)
        logger.info("FCM token deleted: $token")
    }

    fun sendPushNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ): Boolean {
        return try {
            val notification =
                Notification
                    .builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()

            val messageBuilder =
                Message
                    .builder()
                    .setToken(token)
                    .setNotification(notification)

            if (data.isNotEmpty()) {
                messageBuilder.putAllData(data)
            }

            val message = messageBuilder.build()
            val response = FirebaseMessaging.getInstance().send(message)

            logger.info("Successfully sent message: $response")
            true
        } catch (e: Exception) {
            logger.error("Error sending FCM message to token: $token", e)
            false
        }
    }

    fun sendPushNotificationToUser(
        user: User,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ) {
        val tokens = fcmTokenRepository.findByUser(user)
        if (tokens.isEmpty()) {
            logger.warn("No FCM tokens found for user: ${user.id}")
            return
        }

        tokens.forEach { fcmToken ->
            val success = sendPushNotification(fcmToken.token, title, body, data)
            if (!success) {
                logger.warn("Failed to send notification to token: ${fcmToken.token}")
            }
        }
    }

    fun sendPushNotificationToUsers(
        users: List<User>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ) {
        users.forEach { user ->
            sendPushNotificationToUser(user, title, body, data)
        }
    }

    fun getTokensByUser(user: User): List<FcmTokenResponse> {
        val tokens = fcmTokenRepository.findByUser(user)
        return tokens.map {
            FcmTokenResponse.from(it)
        }
    }

    @Transactional(readOnly = true)
    fun getNotificationHistory(user: User): List<NotificationHistoryResponse> {
        val histories = notificationHistoryRepository.findAllByUserOrderByCreatedAtDesc(user)
        return histories.map {
            NotificationHistoryResponse.from(it)
        }
    }
}
