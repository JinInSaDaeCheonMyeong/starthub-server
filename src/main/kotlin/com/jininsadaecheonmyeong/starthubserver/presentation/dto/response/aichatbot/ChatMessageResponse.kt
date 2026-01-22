package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.enums.aichatbot.MessageRole
import java.time.LocalDateTime

data class ChatMessageResponse(
    val id: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(message: AIChatMessage): ChatMessageResponse {
            return ChatMessageResponse(
                id = message.id!!,
                role = message.role,
                content = message.content,
                createdAt = message.createdAt,
            )
        }
    }
}
