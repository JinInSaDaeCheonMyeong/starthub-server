package com.jininsadaecheonmyeong.starthubserver.domain.repository.chat

import com.jininsadaecheonmyeong.starthubserver.domain.entity.chat.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByRoomIdOrderBySentAtAsc(roomId: Long): List<ChatMessage>
}
