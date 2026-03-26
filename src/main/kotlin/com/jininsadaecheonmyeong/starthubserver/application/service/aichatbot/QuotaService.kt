package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.ChatQuotaUsage
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot.QuotaExceededException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot.ChatQuotaUsageRepository
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class QuotaService(
    private val chatQuotaUsageRepository: ChatQuotaUsageRepository,
) {
    private val log = logger()

    @Transactional(readOnly = true)
    fun checkQuota(user: User) {
        val tier = user.tier
        if (tier.unlimited) return

        val now = LocalDateTime.now()
        val weekStart = getWeekStart(now.toLocalDate())

        val weeklyTokens = chatQuotaUsageRepository.getWeeklyTotalTokens(user, weekStart)
        if (weeklyTokens >= tier.weeklyTokenLimit) {
            val weekEnd = weekStart.plusWeeks(1).atStartOfDay()
            throw QuotaExceededException(
                "주간 사용량을 초과했습니다. ${weekEnd.toLocalDate()}부터 다시 사용할 수 있습니다.",
                resetAt = weekEnd,
            )
        }

        val windowCutoff = now.minusHours(WINDOW_HOURS)
        val currentUsage = chatQuotaUsageRepository.findByUserAndWindowStartedAtAfter(user, windowCutoff)
        if (currentUsage != null && currentUsage.totalTokens >= tier.windowTokenLimit) {
            val resetAt = currentUsage.windowStartedAt.plusHours(WINDOW_HOURS)
            throw QuotaExceededException(
                "사용량을 초과했습니다. ${formatResetTime(resetAt)}부터 다시 사용할 수 있습니다.",
                resetAt = resetAt,
            )
        }
    }

    @Transactional
    fun recordUsage(
        user: User,
        inputTokens: Int,
        outputTokens: Int,
    ) {
        if (inputTokens == 0 && outputTokens == 0) return

        val now = LocalDateTime.now()
        val windowCutoff = now.minusHours(WINDOW_HOURS)
        val weekStart = getWeekStart(now.toLocalDate())

        val existingUsage = chatQuotaUsageRepository.findByUserAndWindowStartedAtAfter(user, windowCutoff)

        if (existingUsage != null) {
            existingUsage.addTokens(inputTokens, outputTokens)
            chatQuotaUsageRepository.save(existingUsage)
        } else {
            val newUsage =
                ChatQuotaUsage(
                    user = user,
                    inputTokens = inputTokens,
                    outputTokens = outputTokens,
                    windowStartedAt = now,
                    weekStartedAt = weekStart,
                )
            chatQuotaUsageRepository.save(newUsage)
        }

        log.info(
            "Quota 사용 기록: userId={}, tier={}, input={}, output={}, total={}",
            user.id,
            user.tier,
            inputTokens,
            outputTokens,
            inputTokens + outputTokens,
        )
    }

    @Transactional(readOnly = true)
    fun getQuotaStatus(user: User): QuotaStatusResponse {
        val tier = user.tier
        val now = LocalDateTime.now()
        val weekStart = getWeekStart(now.toLocalDate())
        val windowCutoff = now.minusHours(WINDOW_HOURS)

        val currentUsage = chatQuotaUsageRepository.findByUserAndWindowStartedAtAfter(user, windowCutoff)
        val windowTokensUsed = currentUsage?.totalTokens ?: 0
        val windowResetAt = currentUsage?.windowStartedAt?.plusHours(WINDOW_HOURS)

        val weeklyTokensUsed = chatQuotaUsageRepository.getWeeklyTotalTokens(user, weekStart)
        val weekResetAt = weekStart.plusWeeks(1).atStartOfDay()

        return QuotaStatusResponse(
            tier = tier.name,
            unlimited = tier.unlimited,
            windowTokensUsed = windowTokensUsed,
            windowTokenLimit = tier.windowTokenLimit,
            windowTokensRemaining = if (tier.unlimited) -1 else maxOf(0, tier.windowTokenLimit - windowTokensUsed),
            windowResetAt = windowResetAt,
            weeklyTokensUsed = weeklyTokensUsed,
            weeklyTokenLimit = tier.weeklyTokenLimit,
            weeklyTokensRemaining = if (tier.unlimited) -1 else maxOf(0, tier.weeklyTokenLimit - weeklyTokensUsed),
            weeklyResetAt = weekResetAt,
        )
    }

    private fun getWeekStart(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    private fun formatResetTime(resetAt: LocalDateTime): String {
        return "${resetAt.monthValue}월 ${resetAt.dayOfMonth}일 ${resetAt.hour}시 ${resetAt.minute}분"
    }

    companion object {
        const val WINDOW_HOURS = 5L
    }
}

data class QuotaStatusResponse(
    val tier: String,
    val unlimited: Boolean,
    val windowTokensUsed: Int,
    val windowTokenLimit: Int,
    val windowTokensRemaining: Int,
    val windowResetAt: LocalDateTime?,
    val weeklyTokensUsed: Int,
    val weeklyTokenLimit: Int,
    val weeklyTokensRemaining: Int,
    val weeklyResetAt: LocalDateTime,
)
