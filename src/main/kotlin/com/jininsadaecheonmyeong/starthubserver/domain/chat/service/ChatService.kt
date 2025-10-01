package com.jininsadaecheonmyeong.starthubserver.domain.chat.service

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatRoomResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.CreateChatMessageDto
import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.toResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.exception.ChatRoomNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.chat.repository.ChatMessageRepository
import com.jininsadaecheonmyeong.starthubserver.domain.chat.repository.ChatRoomRepository
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.repository.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
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
) {
    @Transactional
    fun getOrCreateChatRoom(
        userId: Long,
        companyId: Long,
    ): ChatRoomResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
        val company = companyRepository.findById(companyId).orElseThrow { CompanyNotFoundException("찾을 수 없는 기업") }
        val founder = company.founder

        val room =
            chatRoomRepository.findChatRoomByUsers(user, founder)
                ?: chatRoomRepository.save(ChatRoom(user1 = user, user2 = founder))

        return ChatRoomResponse(room.id, userId, founder.id!!)
    }

    @Transactional
    fun saveAndSendMessage(createChatMessageDto: CreateChatMessageDto): ChatMessageResponse {
        val room =
            chatRoomRepository.findById(createChatMessageDto.roomId)
                .orElseThrow { ChatRoomNotFoundException("찾을 수 없는 채팅방") }
        val sender =
            userRepository.findById(createChatMessageDto.senderId)
                .orElseThrow { UserNotFoundException("찾을 수 없는 유저") }

        val chatMessage =
            ChatMessage(
                room = room,
                sender = sender,
                message = createChatMessageDto.message,
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
