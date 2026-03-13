package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.ReferenceParser
import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.enums.aichatbot.MessageRole
import java.time.LocalDateTime

data class ChatMessageResponse(
    val id: Long,
    val role: MessageRole,
    val content: String,
    val references: List<ReferenceResponse>? = null,
    val createdAt: LocalDateTime,
) {
    companion object {
        private val referenceParser = ReferenceParser()

        fun from(message: AIChatMessage): ChatMessageResponse {
            if (message.role == MessageRole.ASSISTANT) {
                val parseResult = referenceParser.parseAndClean(message.content)
                val refs = parseResult.references.ifEmpty { null }
                return ChatMessageResponse(
                    id = message.id!!,
                    role = message.role,
                    content = parseResult.cleanedContent,
                    references = refs?.map { ReferenceResponse.from(it) },
                    createdAt = message.createdAt,
                )
            }
            return ChatMessageResponse(
                id = message.id!!,
                role = message.role,
                content = message.content,
                createdAt = message.createdAt,
            )
        }
    }
}
