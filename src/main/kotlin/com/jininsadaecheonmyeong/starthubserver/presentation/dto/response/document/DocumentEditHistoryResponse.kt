package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentEditHistory
import java.time.LocalDateTime

data class DocumentEditHistoryResponse(
    val id: Long,
    val description: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(history: DocumentEditHistory) =
            DocumentEditHistoryResponse(
                id = history.id!!,
                description = history.description,
                createdAt = history.createdAt,
            )
    }
}
