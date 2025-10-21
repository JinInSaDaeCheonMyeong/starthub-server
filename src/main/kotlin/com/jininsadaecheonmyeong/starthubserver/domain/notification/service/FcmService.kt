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
    ): Result<String> =
        runCatching {
            message {
                setToken(token)
                setNotification(
                    notification {
                        setTitle(title)
                        setBody(body)
                    },
                )
                data.takeIf { it.isNotEmpty() }?.let { putAllData(it) }
            }.let { FirebaseMessaging.getInstance().send(it) }
        }.onSuccess {
            logger.info("Successfully sent message: $it")
        }.onFailure {
            logger.error("Error sending FCM message to token: $token", it)
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
            sendPushNotification(fcmToken.token, title, body, data)
                .onFailure {
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

private inline fun notification(block: Notification.Builder.() -> Unit): Notification = Notification.builder().apply(block).build()

private inline fun message(block: Message.Builder.() -> Unit): Message = Message.builder().apply(block).build()
