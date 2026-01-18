package com.jininsadaecheonmyeong.starthubserver.presentation.controller.chat

import com.jininsadaecheonmyeong.starthubserver.application.usecase.chat.ChatUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.chat.ChatDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.chat.CreateChatMessageRequest
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatUseCase: ChatUseCase,
) : ChatDocs {
    @PostMapping("/room")
    override fun createOrGetRoom(
        @RequestParam userId: Long,
        @RequestParam companyId: Long,
    ) = BaseResponse.of(chatUseCase.getOrCreateChatRoom(userId, companyId), "채팅방 생성 성공")

    @GetMapping("/messages")
    override fun getMessages(
        @RequestParam roomId: Long,
    ) = BaseResponse.of(chatUseCase.getMessages(roomId), "채팅 내역 조회 성공")

    @GetMapping("/my")
    override fun getUserChatRooms() = BaseResponse.of(chatUseCase.getMyChatRooms(), "나의 채팅방 목록 조회 성공")

    @MessageMapping("/send")
    fun saveAndSendMessage(
        @Payload createChatMessageRequest: CreateChatMessageRequest,
    ) = BaseResponse.of(chatUseCase.saveAndSendMessage(createChatMessageRequest), "성공")
}
