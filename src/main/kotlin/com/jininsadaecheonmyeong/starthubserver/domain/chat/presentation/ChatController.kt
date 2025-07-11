package com.jininsadaecheonmyeong.starthubserver.domain.chat.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.CreateChatMessageDto
import com.jininsadaecheonmyeong.starthubserver.domain.chat.docs.ChatDocs
import com.jininsadaecheonmyeong.starthubserver.domain.chat.service.ChatService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
) : ChatDocs {
    @PostMapping("/room")
    override fun createOrGetRoom(
        @RequestParam userId: UUID,
        @RequestParam companyId: Long,
    ) = BaseResponse.of(chatService.getOrCreateChatRoom(userId, companyId), "채팅방 생성 성공")

    @GetMapping("/messages")
    override fun getMessages(
        @RequestParam roomId: Long,
    ) = BaseResponse.of(chatService.getMessages(roomId), "채팅 내역 조회 성공")

    @MessageMapping("/send")
    fun saveAndSendMessage(
        @Payload createChatMessageDto: CreateChatMessageDto,
    ) = BaseResponse.of(chatService.saveAndSendMessage(createChatMessageDto), "성공")
}
