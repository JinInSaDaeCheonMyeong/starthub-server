package com.jininsadaecheonmyeong.starthubserver.domain.chat.repository

import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByRoomIdOrderBySentAtAsc(roomId: Long): List<ChatMessage>
}