package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

import java.util.UUID

data class CreateChatMessageDto(
    val roomId: Long,
    val senderId: UUID,
    val message: String,
)
