package com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "chat_quota_usages")
class ChatQuotaUsage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var inputTokens: Int = 0,
    @Column(nullable = false)
    var outputTokens: Int = 0,
    @Column(nullable = false)
    val windowStartedAt: LocalDateTime,
    @Column(nullable = false)
    val weekStartedAt: LocalDate,
) : BaseEntity() {
    val totalTokens: Int
        get() = inputTokens + outputTokens

    fun addTokens(
        input: Int,
        output: Int,
    ) {
        this.inputTokens += input
        this.outputTokens += output
    }
}
