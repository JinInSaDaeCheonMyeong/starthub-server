package com.jininsadaecheonmyeong.starthubserver.domain.chat.data

import java.util.UUID

data class ChatRoomResponse(
    val id: Long,
    val user1Id: UUID,
    val user2Id: UUID,
)
