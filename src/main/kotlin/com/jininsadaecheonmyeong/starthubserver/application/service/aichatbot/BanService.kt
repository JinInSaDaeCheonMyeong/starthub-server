package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.discord.DiscordWebhookService
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BanService(
    private val userRepository: UserRepository,
    private val discordWebhookService: DiscordWebhookService,
    private val rateLimitService: RateLimitService,
) {
    private val log = logger()

    @Transactional
    fun checkAndBanIfAbusive(
        user: User,
        ip: String,
    ): Boolean {
        val violations = rateLimitService.getViolationCount(user.id!!)
        if (violations >= RateLimitService.BAN_THRESHOLD) {
            banUser(user, ip, "1시간 내 Rate Limit 위반 ${violations}회 (임계값: ${RateLimitService.BAN_THRESHOLD}회)")
            return true
        }
        return false
    }

    @Transactional
    fun banUser(
        user: User,
        ip: String,
        reason: String,
    ) {
        user.ban(reason)
        userRepository.save(user)

        log.warn("사용자 차단: userId={}, username={}, ip={}, reason={}", user.id, user.username, ip, reason)

        sendBanNotification(user, ip, reason)
    }

    private fun sendBanNotification(
        user: User,
        ip: String,
        reason: String,
    ) {
        try {
            discordWebhookService.sendErrorNotification(
                error = RuntimeException("사용자 자동 차단"),
                requestUri = "/chatbot (자동 차단)",
                userId = user.id.toString(),
                additionalInfo =
                    mapOf(
                        "차단 유형" to "자동 차단 (Rate Limit 반복 위반)",
                        "사용자 이름" to (user.username ?: "미설정"),
                        "사용자 이메일" to user.email,
                        "클라이언트 IP" to ip,
                        "차단 사유" to reason,
                    ),
            )
        } catch (e: Exception) {
            log.error("차단 Discord 알림 전송 실패", e)
        }
    }
}
