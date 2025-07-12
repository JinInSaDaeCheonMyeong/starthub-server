package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

data class ChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val message: String,
    val sentAt: String,
)
