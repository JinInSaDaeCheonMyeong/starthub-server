package com.jininsadaecheonmyeong.starthubserver.domain.chat.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.CreateChatMessageDto
import com.jininsadaecheonmyeong.starthubserver.domain.chat.docs.ChatDocs
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
) : ChatDocs {
    @PostMapping("/room")
    override fun createOrGetRoom(@RequestParam user1Id: UUID, @RequestParam user2Id: UUID)
        = BaseResponse.of(chatService.getOrCreateChatRoom(user1Id, user2Id), "채팅방 생성 성공")

    @GetMapping("/messages")
    override fun getMessages(@RequestParam roomId: Long)
        = BaseResponse.of(chatService.getMessages(roomId), "채팅 내역 조회 성공")

    @MessageMapping("/send")
    fun saveAndSendMessage(@Payload createChatMessageDto: CreateChatMessageDto)
        = BaseResponse.of(chatService.saveAndSendMessage(createChatMessageDto), "성공")
}