package com.jininsadaecheonmyeong.starthubserver.domain.chat.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
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

    val sentAt: LocalDateTime = LocalDateTime.now()
)