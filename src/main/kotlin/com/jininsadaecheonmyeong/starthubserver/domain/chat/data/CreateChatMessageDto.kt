package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

import java.util.*

data class CreateChatMessageDto(
    val roomId: Long,
    val senderId: UUID,
    val message: String
)