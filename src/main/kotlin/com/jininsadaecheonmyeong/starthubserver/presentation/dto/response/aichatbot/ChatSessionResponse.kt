package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatSession
import java.time.LocalDateTime

data class ChatSessionResponse(
    val id: Long,
    val title: String,
    val messageCount: Int,
    val hasDocuments: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(session: AIChatSession): ChatSessionResponse {
            return ChatSessionResponse(
                id = session.id!!,
                title = session.title,
                messageCount = session.messages.size,
                hasDocuments = session.hasDocuments(),
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }

        fun fromList(sessions: List<AIChatSession>): List<ChatSessionResponse> {
            return sessions.map { from(it) }
        }
    }
}
