package com.jininsadaecheonmyeong.starthubserver.dto.request.chat

data class CreateChatMessageRequest(
    val roomId: Long,
    val senderId: Long,
    val message: String,
)
