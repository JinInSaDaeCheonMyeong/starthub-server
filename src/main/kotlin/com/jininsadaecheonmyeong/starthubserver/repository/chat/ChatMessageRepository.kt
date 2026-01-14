package com.jininsadaecheonmyeong.starthubserver.repository.chat

import com.jininsadaecheonmyeong.starthubserver.entity.chat.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByRoomIdOrderBySentAtAsc(roomId: Long): List<ChatMessage>
}
