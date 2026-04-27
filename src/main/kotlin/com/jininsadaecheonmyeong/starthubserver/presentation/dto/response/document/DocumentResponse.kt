package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentStatus
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentType
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType
import java.time.LocalDateTime

data class DocumentResponse(
    val id: Long,
    val title: String,
    val documentType: DocumentType,
    val toneType: ToneType?,
    val content: String?,
    val wordCount: Int,
    val status: DocumentStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(document: GeneratedDocument) = DocumentResponse(
            id = document.id!!,
            title = document.title,
            documentType = document.documentType,
            toneType = document.toneType,
            content = document.content,
            wordCount = document.wordCount,
            status = document.status,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
        )
    }
}