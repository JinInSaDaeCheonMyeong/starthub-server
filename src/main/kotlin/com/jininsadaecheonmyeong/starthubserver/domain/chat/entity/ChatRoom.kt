package com.jininsadaecheonmyeong.starthubserver.domain.chat.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "chat_rooms")
data class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    val user1: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    val user2: User,
)
