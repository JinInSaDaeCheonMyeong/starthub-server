package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.chat

data class CreateChatMessageRequest(
    val roomId: Long,
    val senderId: Long,
    val message: String,
)
