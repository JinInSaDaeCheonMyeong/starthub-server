package com.jininsadaecheonmyeong.starthubserver.domain.repository.chat

import com.jininsadaecheonmyeong.starthubserver.domain.entity.chat.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {
    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.user1 = :user1 AND cr.user2 = :user2) OR (cr.user1 = :user2 AND cr.user2 = :user1)")
    fun findChatRoomByUsers(
        user1: User,
        user2: User,
    ): ChatRoom?

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1 = :user OR cr.user2 = :user")
    fun findChatRoomsByUser(user: User): List<ChatRoom>
}
