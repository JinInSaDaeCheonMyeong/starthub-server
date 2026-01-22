package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot.AIChatDocument
import java.time.LocalDateTime

data class ChatDocumentResponse(
    val id: Long,
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val isImage: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(document: AIChatDocument): ChatDocumentResponse {
            return ChatDocumentResponse(
                id = document.id!!,
                fileName = document.fileName,
                fileUrl = document.fileUrl,
                fileType = document.fileType,
                isImage = document.isImage(),
                createdAt = document.createdAt,
            )
        }
    }
}
