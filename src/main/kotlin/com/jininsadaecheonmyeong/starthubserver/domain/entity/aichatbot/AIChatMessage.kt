package com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.enums.aichatbot.MessageRole
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ai_chat_messages")
class AIChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: AIChatSession,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: MessageRole,
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    val content: String,
    @Column(nullable = true)
    val tokenCount: Int? = null,
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AIChatMessage) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
