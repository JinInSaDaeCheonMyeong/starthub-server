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
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @Transactional
    fun getOrCreateChatRoom(
        user1Id: UUID,
        user2Id: UUID,
    ): ChatRoomResponse {
        val user1 = userRepository.findById(user1Id).orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
        val user2 = userRepository.findById(user2Id).orElseThrow { UserNotFoundException("찾을 수 없는 유저") }

        val room =
            chatRoomRepository.findChatRoomByUsers(user1, user2)
                ?: chatRoomRepository.save(ChatRoom(user1 = user1, user2 = user2))

        return ChatRoomResponse(room.id, room.user1.id!!, room.user2.id!!)
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
}
