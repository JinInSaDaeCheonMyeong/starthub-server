package com.jininsadaecheonmyeong.starthubserver.domain.chat.entity

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    val room: ChatRoom,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    val sender: User,
    val message: String,
    val sentAt: LocalDateTime = LocalDateTime.now(),
)

fun ChatMessage.toResponse(): ChatMessageResponse =
    ChatMessageResponse(
        id = this.id,
        roomId = this.room.id,
        senderId = this.sender.id!!,
        message = this.message,
        sentAt = this.sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
