package com.jininsadaecheonmyeong.starthubserver.domain.entity.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "ai_chat_sessions")
class AIChatSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var title: String = "New Chat",
    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @BatchSize(size = 100)
    val messages: MutableSet<AIChatMessage> = mutableSetOf(),
    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 50)
    val documents: MutableSet<AIChatDocument> = mutableSetOf(),
    @Column(nullable = false)
    var deleted: Boolean = false,
) : BaseEntity() {
    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun updateTitle(newTitle: String) {
        this.title = newTitle
    }

    fun delete() {
        this.deleted = true
    }

    fun hasDocuments(): Boolean = documents.isNotEmpty()

    fun addMessage(message: AIChatMessage) {
        messages.add(message)
    }

    fun addDocument(document: AIChatDocument) {
        documents.add(document)
    }

    fun getMessagesSorted(): List<AIChatMessage> = messages.sortedBy { it.createdAt }

    fun getDocumentsSorted(): List<AIChatDocument> = documents.sortedBy { it.createdAt }
}
