package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

import java.util.*

data class ChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderId: UUID,
    val message: String,
    val sentAt: String
)