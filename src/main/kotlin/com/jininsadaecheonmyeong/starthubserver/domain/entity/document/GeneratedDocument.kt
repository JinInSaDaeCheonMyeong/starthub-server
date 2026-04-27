package com.jininsadaecheonmyeong.starthubserver.domain.entity.document

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentStatus
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentType
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType
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
@Table(name = "generated_documents")
class GeneratedDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var title: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var documentType: DocumentType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var toneType: ToneType? = null,
    @Column(columnDefinition = "LONGTEXT")
    var content: String? = null,
    @Column(nullable = false)
    var wordCount: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DocumentStatus = DocumentStatus.DRAFT,
    @Column(nullable = false)
    var deleted: Boolean = false,
) : BaseEntity() {
    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun updateContent(content: String) {
        this.content = content
        this.wordCount = content.length
    }

    fun markAsGenerating() {
        this.status = DocumentStatus.GENERATING
    }

    fun markAsCompleted() {
        this.status = DocumentStatus.COMPLETED
    }

    fun delete() {
        this.deleted = true
    }
}