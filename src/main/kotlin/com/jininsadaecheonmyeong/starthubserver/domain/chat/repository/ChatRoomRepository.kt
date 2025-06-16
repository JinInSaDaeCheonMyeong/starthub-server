package com.jininsadaecheonmyeong.starthubserver.domain.chat.repository

import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {
    fun findByUser1AndUser2(user1: User, user2: User): ChatRoom?
    fun findByUser2AndUser1(user2: User, user1: User): ChatRoom?
}