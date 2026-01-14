package com.jininsadaecheonmyeong.starthubserver.usecase.chat

import com.jininsadaecheonmyeong.starthubserver.dto.request.chat.CreateChatMessageRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.chat.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.chat.ChatRoomResponse

interface ChatUseCase {
    fun getOrCreateChatRoom(
        userId: Long,
        companyId: Long,
    ): ChatRoomResponse

    fun saveAndSendMessage(createChatMessageRequest: CreateChatMessageRequest): ChatMessageResponse

    fun getMessages(roomId: Long): List<ChatMessageResponse>

    fun getMyChatRooms(): List<ChatRoomResponse>
}
