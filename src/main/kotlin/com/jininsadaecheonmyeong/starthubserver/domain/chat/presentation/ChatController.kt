package com.jininsadaecheonmyeong.starthubserver.domain.chat.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.CreateChatMessageDto
import com.jininsadaecheonmyeong.starthubserver.domain.chat.service.ChatService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {
    @PostMapping("/room")
    fun createOrGetRoom(@RequestParam user1Id: UUID, @RequestParam user2Id: UUID)
        = BaseResponse.of(chatService.getOrCreateChatRoom(user1Id, user2Id), "채팅방 생성 성공")

    @GetMapping("/messages")
    fun getMessages(@RequestParam roomId: Long)
        = BaseResponse.of(chatService.getMessages(roomId), "메시지 조회 성공")

    @MessageMapping("/send")
    fun sendMessage(@Payload createChatMessageDto: CreateChatMessageDto)
        = BaseResponse.of(chatService.saveMessage(createChatMessageDto), "메시지 전송 성공")
}