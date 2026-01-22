package com.jininsadaecheonmyeong.starthubserver.application.usecase.chat

import com.jininsadaecheonmyeong.starthubserver.domain.entity.chat.ChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.entity.chat.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.entity.chat.toResponse
import com.jininsadaecheonmyeong.starthubserver.domain.exception.chat.ChatRoomNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.company.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.chat.ChatMessageRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.chat.ChatRoomRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.company.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.chat.CreateChatMessageRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.chat.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.chat.ChatRoomResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ChatUseCase(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val companyRepository: CompanyRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) {
    @Transactional
    fun getOrCreateChatRoom(
        userId: Long,
        companyId: Long,
    ): ChatRoomResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException("찾을 수 없는 유저")
        val company = companyRepository.findByIdOrNull(companyId) ?: throw CompanyNotFoundException("찾을 수 없는 기업")
        val founder = company.founder

        val room =
            chatRoomRepository.findChatRoomByUsers(user, founder)
                ?: chatRoomRepository.save(ChatRoom(user1 = user, user2 = founder))

        return ChatRoomResponse(room.id, userId, founder.id!!)
    }

    @Transactional
    fun saveAndSendMessage(createChatMessageRequest: CreateChatMessageRequest): ChatMessageResponse {
        val room =
            chatRoomRepository.findByIdOrNull(createChatMessageRequest.roomId)
                ?: throw ChatRoomNotFoundException("찾을 수 없는 채팅방")
        val sender =
            userRepository.findByIdOrNull(createChatMessageRequest.senderId)
                ?: throw UserNotFoundException("찾을 수 없는 유저")

        val chatMessage =
            ChatMessage(
                room = room,
                sender = sender,
                message = createChatMessageRequest.message,
            )
        val saved = chatMessageRepository.save(chatMessage)

        val response = saved.toResponse()

        messagingTemplate.convertAndSend("/sub/chat/${room.id}", response)

        return response
    }

    @Transactional(readOnly = true)
    fun getMessages(roomId: Long): List<ChatMessageResponse> {
        val messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId)
        return messages.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getMyChatRooms(): List<ChatRoomResponse> {
        val user = userAuthenticationHolder.current()
        val rooms = chatRoomRepository.findChatRoomsByUser(user)
        return rooms.map { ChatRoomResponse(it.id, it.user1.id!!, it.user2.id!!) }
    }
}
