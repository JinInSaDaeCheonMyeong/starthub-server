package com.jininsadaecheonmyeong.starthubserver.domain.entity.document

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
@Table(name = "document_templates")
class DocumentTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_document_id", nullable = false)
    val document: GeneratedDocument,
    @Column(nullable = false)
    val fileName: String,
    @Column(length = 1024, nullable = false)
    val fileUrl: String,
    @Column(length = 50, nullable = false)
    val fileType: String,
    @Column(columnDefinition = "LONGTEXT")
    val extractedText: String? = null,
) : BaseEntity()
