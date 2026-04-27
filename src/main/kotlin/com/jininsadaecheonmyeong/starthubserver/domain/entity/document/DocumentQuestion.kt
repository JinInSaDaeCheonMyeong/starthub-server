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
@Table(name = "document_questions")
class DocumentQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_document_id", nullable = false)
    val document: GeneratedDocument,
    @Column(nullable = false, length = 500)
    val questionText: String,
    @Column(columnDefinition = "TEXT")
    var answerText: String? = null,
    @Column(nullable = false)
    val orderIndex: Int,
    @Column(nullable = false)
    val required: Boolean = true,
) : BaseEntity() {
    fun updateAnswer(answer: String) {
        this.answerText = answer
    }
}