package com.jininsadaecheonmyeong.starthubserver.dto.response.chat

data class ChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val message: String,
    val sentAt: String,
)
