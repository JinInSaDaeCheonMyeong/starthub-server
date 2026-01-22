package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatSession
import java.time.LocalDateTime

data class ChatSessionDetailResponse(
    val id: Long,
    val title: String,
    val messages: List<ChatMessageResponse>,
    val documents: List<ChatDocumentResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(session: AIChatSession): ChatSessionDetailResponse {
            return ChatSessionDetailResponse(
                id = session.id!!,
                title = session.title,
                messages = session.messages.map { ChatMessageResponse.from(it) },
                documents = session.documents.map { ChatDocumentResponse.from(it) },
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
            )
        }
    }
}
