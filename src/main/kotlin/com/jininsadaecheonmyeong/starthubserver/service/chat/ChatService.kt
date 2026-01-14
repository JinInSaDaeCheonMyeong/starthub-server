package com.jininsadaecheonmyeong.starthubserver.service.chat

import com.jininsadaecheonmyeong.starthubserver.dto.request.chat.CreateChatMessageRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.chat.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.chat.ChatRoomResponse
import com.jininsadaecheonmyeong.starthubserver.entity.chat.ChatMessage
import com.jininsadaecheonmyeong.starthubserver.entity.chat.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.entity.chat.toResponse
import com.jininsadaecheonmyeong.starthubserver.exception.chat.ChatRoomNotFoundException
import com.jininsadaecheonmyeong.starthubserver.exception.company.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.exception.user.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.repository.chat.ChatMessageRepository
import com.jininsadaecheonmyeong.starthubserver.repository.chat.ChatRoomRepository
import com.jininsadaecheonmyeong.starthubserver.repository.company.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.usecase.chat.ChatUseCase
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val companyRepository: CompanyRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : ChatUseCase {
    @Transactional
    override fun getOrCreateChatRoom(
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
    override fun saveAndSendMessage(createChatMessageRequest: CreateChatMessageRequest): ChatMessageResponse {
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
    override fun getMessages(roomId: Long): List<ChatMessageResponse> {
        val messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId)
        return messages.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getMyChatRooms(): List<ChatRoomResponse> {
        val user = userAuthenticationHolder.current()
        val rooms = chatRoomRepository.findChatRoomsByUser(user)
        return rooms.map { ChatRoomResponse(it.id, it.user1.id!!, it.user2.id!!) }
    }
}
