package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

import java.util.UUID

data class ChatRoomResponse(
    val id: Long,
    val userId: UUID,
    val founderId: UUID,
)
