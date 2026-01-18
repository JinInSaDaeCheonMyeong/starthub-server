package com.jininsadaecheonmyeong.starthubserver.application.service.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.jininsadaecheonmyeong.starthubserver.application.usecase.notification.NotificationUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.entity.notification.FcmToken
import com.jininsadaecheonmyeong.starthubserver.domain.entity.notification.NotificationHistory
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.enums.notification.DeviceType
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.notification.FcmTokenRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.notification.NotificationHistoryRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.schedule.ScheduleRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserStartupFieldRepository
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.BmcInfo
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.LikedAnnouncementUrl
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.LikedAnnouncementsContent
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.RecommendationRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.RecommendationResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.notification.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.notification.NotificationHistoryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class NotificationService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
    private val userRepository: UserRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val announcementRepository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val scheduleRepository: ScheduleRepository,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val webClient: WebClient,
    @param:Value("\${recommendation.fastapi-url}") private val fastapiUrl: String,
) : NotificationUseCase {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    companion object {
        private val AI_RECOMMENDATION_TITLES =
            listOf(
                "✨ %s 님, 딱 맞는 지원사업이 도착했어요!",
                "🤖 오늘의 맞춤 추천 👉 글로벌 진출 지원사업",
                "📌 회원님 기업에 최적화된 지원사업을 찾았습니다!",
            )

        private val INTEREST_CATEGORY_TITLES =
            listOf(
                "💰 [%s] 새 지원 공고가 올라왔습니다. 확인해보세요!",
                "🎓 [%s] 분야 신규 지원 소식이 있어요!",
                "🎉 놓치면 아쉬운 [%s] 지원 소식이 도착했어요!",
            )

        private const val NOTIFICATION_TYPE_AI_RECOMMENDATION = "AI_RECOMMENDATION"
        private const val NOTIFICATION_TYPE_INTEREST_CATEGORY = "INTEREST_CATEGORY"
        private const val NOTIFICATION_TYPE_DEADLINE = "DEADLINE"
    }

    @Transactional
    override fun registerToken(
        user: User,
        token: String,
        deviceType: DeviceType,
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
    override fun deleteToken(token: String) {
        fcmTokenRepository.deleteByToken(token)
        logger.info("FCM token deleted: $token")
    }

    override fun sendPushNotificationToUser(
        user: User,
        title: String,
        body: String,
        data: Map<String, String>,
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

    override fun getTokensByUser(user: User): List<FcmTokenResponse> {
        val tokens = fcmTokenRepository.findByUser(user)
        return tokens.map { FcmTokenResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getNotificationHistory(user: User): List<NotificationHistoryResponse> {
        val histories = notificationHistoryRepository.findAllByUserOrderByCreatedAtDesc(user)
        return histories.map { NotificationHistoryResponse.from(it) }
    }

    private fun sendPushNotification(
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

    private fun sendPushNotificationToUsers(
        users: List<User>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ) {
        users.forEach { user ->
            sendPushNotificationToUser(user, title, body, data)
        }
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional
    fun sendAiRecommendationNotifications() {
        val users = userRepository.findAll()
        val today = LocalDate.now().atStartOfDay()

        val newAnnouncements =
            announcementRepository.findAllByStatus(AnnouncementStatus.ACTIVE)
                .filter {
                    val createdDate = it.createdAt.toLocalDate()
                    createdDate == today.toLocalDate()
                }

        if (newAnnouncements.isEmpty()) {
            return
        }

        users.forEach { user ->
            try {
                val userInterests = userStartupFieldRepository.findByUser(user)
                if (userInterests.isEmpty()) {
                    return@forEach
                }

                val recommendedAnnouncements = getRecommendedAnnouncementsForUser(user)
                val todayRecommendations =
                    recommendedAnnouncements
                        .filter { recommended ->
                            newAnnouncements.any { it.id == recommended.id }
                        }.take(1)
                todayRecommendations.forEach { announcement ->
                    if (!notificationHistoryRepository.existsByUserAndAnnouncementAndNotificationType(
                            user,
                            announcement,
                            NOTIFICATION_TYPE_AI_RECOMMENDATION,
                        )
                    ) {
                        val titleTemplate = AI_RECOMMENDATION_TITLES.random()
                        val title = titleTemplate.format(user.username ?: "회원")
                        val body = announcement.title
                        val data =
                            mapOf(
                                "announcementId" to announcement.id.toString(),
                                "channelId" to NOTIFICATION_TYPE_AI_RECOMMENDATION,
                            )

                        sendPushNotificationToUser(user, title, body, data)
                        notificationHistoryRepository.save(
                            NotificationHistory(
                                user = user,
                                announcement = announcement,
                                notificationType = NOTIFICATION_TYPE_AI_RECOMMENDATION,
                                title = title,
                                body = body,
                                isSent = true,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending AI recommendation notification to user ${user.id}", e)
            }
        }
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    fun sendInterestCategoryNotifications() {
        val users = userRepository.findAll()
        val today = LocalDate.now().atStartOfDay()
        val newAnnouncements =
            announcementRepository.findAllByStatus(AnnouncementStatus.ACTIVE)
                .filter {
                    val createdDate = it.createdAt.toLocalDate()
                    createdDate == today.toLocalDate()
                }

        if (newAnnouncements.isEmpty()) {
            return
        }

        users.forEach { user ->
            try {
                val userInterests = userStartupFieldRepository.findByUser(user)
                if (userInterests.isEmpty()) {
                    return@forEach
                }

                val interestNames = userInterests.map { it.businessType.displayName }

                newAnnouncements.forEach { announcement ->
                    val matchedInterest =
                        interestNames.find { interest ->
                            announcement.supportField.contains(interest, ignoreCase = true)
                        }

                    if (matchedInterest != null) {
                        if (!notificationHistoryRepository.existsByUserAndAnnouncementAndNotificationType(
                                user,
                                announcement,
                                NOTIFICATION_TYPE_INTEREST_CATEGORY,
                            )
                        ) {
                            val titleTemplate = INTEREST_CATEGORY_TITLES.random()
                            val title = titleTemplate.format(matchedInterest)
                            val body = announcement.title
                            val data =
                                mapOf(
                                    "announcementId" to announcement.id.toString(),
                                    "channelId" to NOTIFICATION_TYPE_INTEREST_CATEGORY,
                                )

                            sendPushNotificationToUser(user, title, body, data)
                            notificationHistoryRepository.save(
                                NotificationHistory(
                                    user = user,
                                    announcement = announcement,
                                    notificationType = NOTIFICATION_TYPE_INTEREST_CATEGORY,
                                    title = title,
                                    body = body,
                                    isSent = true,
                                ),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending interest category notification to user ${user.id}", e)
            }
        }

        logger.info("Completed interest category notifications")
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    fun sendDeadlineNotifications() {
        logger.info("Starting deadline notifications")

        val users = userRepository.findAll()
        val today = LocalDate.now()

        users.forEach { user ->
            try {
                val schedules = scheduleRepository.findAllByUser(user)
                schedules.forEach { schedule ->
                    val endDate = schedule.endDate
                    val daysUntilDeadline = ChronoUnit.DAYS.between(today, endDate)

                    val shouldNotify =
                        when (daysUntilDeadline) {
                            14L, 7L, 3L, 1L, 0L -> true
                            else -> false
                        }

                    if (shouldNotify) {
                        val notificationType = "$NOTIFICATION_TYPE_DEADLINE-$daysUntilDeadline"

                        if (!notificationHistoryRepository.existsByUserAndAnnouncementAndNotificationType(
                                user,
                                schedule.announcement,
                                notificationType,
                            )
                        ) {
                            val title =
                                when (daysUntilDeadline) {
                                    14L, 7L -> "⏰ D-$daysUntilDeadline! [${schedule.announcement.title}] 신청 마감이 다가옵니다."
                                    3L, 1L -> {
                                        val username = user.username ?: "회원"
                                        val field = schedule.announcement.supportField
                                        "🚨 D-$daysUntilDeadline! $username 님이 찜한 [$field] 지원사업 곧 마감돼요."
                                    }
                                    0L -> "🔔 마감 임박! 아직 신청 안 하셨나요?"
                                    else -> "⏰ 마감 알림"
                                }

                            val body = schedule.announcement.title
                            val data =
                                mapOf(
                                    "announcementId" to schedule.announcement.id.toString(),
                                    "channelId" to NOTIFICATION_TYPE_DEADLINE,
                                )

                            sendPushNotificationToUser(user, title, body, data)
                            notificationHistoryRepository.save(
                                NotificationHistory(
                                    user = user,
                                    announcement = schedule.announcement,
                                    notificationType = notificationType,
                                    title = title,
                                    body = body,
                                    isSent = true,
                                ),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending deadline notification to user ${user.id}", e)
            }
        }

        logger.info("Completed deadline notifications")
    }

    private fun getRecommendedAnnouncementsForUser(user: User): List<Announcement> {
        return try {
            val userInterests = userStartupFieldRepository.findByUser(user)
            val interestNames = userInterests.map { it.businessType.displayName }

            if (interestNames.isEmpty()) {
                return emptyList()
            }

            val likedAnnouncements = announcementLikeRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged())
            val likedUrls = likedAnnouncements.map { LikedAnnouncementUrl(it.announcement.url) }.toList()
            val likedContent = LikedAnnouncementsContent(content = likedUrls)

            val bmcs = businessModelCanvasRepository.findAllByUserAndDeletedFalse(user)
            val bmcInfos =
                bmcs.map {
                    BmcInfo(
                        customerSegments = it.customerSegments,
                        valueProposition = it.valueProposition,
                        channels = it.channels,
                        customerRelationships = it.customerRelationships,
                        revenueStreams = it.revenueStreams,
                        keyResources = it.keyResources,
                        keyActivities = it.keyActivities,
                        keyPartners = it.keyPartners,
                        costStructure = it.costStructure,
                    )
                }

            val request =
                RecommendationRequest(
                    interests = interestNames,
                    likedAnnouncements = likedContent,
                    bmcs = bmcInfos,
                )
            val recommendationResponse =
                webClient.post()
                    .uri("$fastapiUrl/recommend")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RecommendationResponse::class.java)
                    .block()

            if (recommendationResponse == null || recommendationResponse.recommendations.isEmpty()) {
                return emptyList()
            }

            val recommendedTitles = recommendationResponse.recommendations.map { it.title }
            announcementRepository.findAllByTitleIn(recommendedTitles)
        } catch (e: Exception) {
            logger.error("Error getting recommendations for user ${user.id}", e)
            emptyList()
        }
    }
}

private inline fun notification(block: Notification.Builder.() -> Unit): Notification = Notification.builder().apply(block).build()

private inline fun message(block: Message.Builder.() -> Unit): Message = Message.builder().apply(block).build()
