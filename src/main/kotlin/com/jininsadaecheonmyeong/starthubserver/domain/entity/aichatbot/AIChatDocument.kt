package com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot

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

@Entity
@Table(name = "ai_chat_documents")
class AIChatDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: AIChatSession,
    @Column(nullable = false)
    val fileName: String,
    @Column(length = 1024, nullable = false)
    val fileUrl: String,
    @Column(length = 50, nullable = false)
    val fileType: String,
    @Column(nullable = true)
    val pineconeDocId: String? = null,
    @Column(columnDefinition = "LONGTEXT", nullable = true)
    val extractedText: String? = null,
) : BaseEntity() {
    fun isImage(): Boolean = fileType.lowercase() in listOf("png", "jpg", "jpeg", "gif", "webp")

    fun isPdf(): Boolean = fileType.lowercase() == "pdf"

    fun isDocx(): Boolean = fileType.lowercase() in listOf("docx", "doc")
}
