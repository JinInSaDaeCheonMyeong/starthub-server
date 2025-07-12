package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

data class CreateChatMessageDto(
    val roomId: Long,
    val senderId: Long,
    val message: String,
)
