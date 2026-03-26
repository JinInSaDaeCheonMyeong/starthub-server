package com.jininsadaecheonmyeong.starthubserver.domain.repository.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.ChatQuotaUsage
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface ChatQuotaUsageRepository : JpaRepository<ChatQuotaUsage, Long> {
    fun findByUserAndWindowStartedAtAfter(
        user: User,
        windowStartedAt: LocalDateTime,
    ): ChatQuotaUsage?

    @Query(
        "SELECT COALESCE(SUM(c.inputTokens + c.outputTokens), 0) " +
            "FROM ChatQuotaUsage c WHERE c.user = :user AND c.weekStartedAt = :weekStart",
    )
    fun getWeeklyTotalTokens(
        @Param("user") user: User,
        @Param("weekStart") weekStart: LocalDate,
    ): Int

    fun findTopByUserOrderByWindowStartedAtDesc(user: User): ChatQuotaUsage?

    @Query(
        "SELECT c FROM ChatQuotaUsage c WHERE c.user = :user AND c.weekStartedAt = :weekStart " +
            "ORDER BY c.windowStartedAt DESC",
    )
    fun findByUserAndWeekStartedAt(
        @Param("user") user: User,
        @Param("weekStart") weekStart: LocalDate,
    ): List<ChatQuotaUsage>
}
