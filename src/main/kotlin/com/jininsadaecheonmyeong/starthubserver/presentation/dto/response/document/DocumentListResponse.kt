package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentStatus
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentType
import java.time.LocalDateTime

data class DocumentListResponse(
    val id: Long,
    val title: String,
    val documentType: DocumentType,
    val status: DocumentStatus,
    val wordCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(document: GeneratedDocument) = DocumentListResponse(
            id = document.id!!,
            title = document.title,
            documentType = document.documentType,
            status = document.status,
            wordCount = document.wordCount,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
        )
    }
}