package com.jininsadaecheonmyeong.starthubserver.domain.chat.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import jakarta.persistence.*

@Entity
data class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    val user1: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    val user2: User
)