package com.jininsadaecheonmyeong.starthubserver.domain.chat.service

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatRoomResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.CreateChatMessageDto
import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatMessage
import com.jininsadaecheonmyeong.starthubserver.domain.chat.entity.ChatRoom
import com.jininsadaecheonmyeong.starthubserver.domain.chat.repository.ChatMessageRepository
import com.jininsadaecheonmyeong.starthubserver.domain.chat.repository.ChatRoomRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun getOrCreateChatRoom(user1Id: UUID, user2Id: UUID): ChatRoomResponse {
        val user1 = userRepository.findById(user1Id).orElseThrow { IllegalArgumentException("User1 not found") }
        val user2 = userRepository.findById(user2Id).orElseThrow { IllegalArgumentException("User2 not found") }

        val existingRoom = chatRoomRepository.findByUser1AndUser2(user1, user2)
            ?: chatRoomRepository.findByUser2AndUser1(user1, user2)

        val room = existingRoom ?: chatRoomRepository.save(ChatRoom(user1 = user1, user2 = user2))

        return ChatRoomResponse(room.id, room.user1.id!!, room.user2.id!!)
    }

    @Transactional
    fun saveMessage(createChatMessageDto: CreateChatMessageDto): ChatMessageResponse {
        val room = chatRoomRepository.findById(createChatMessageDto.roomId).orElseThrow { IllegalArgumentException("ChatRoom not found") }
        val sender = userRepository.findById(createChatMessageDto.senderId).orElseThrow { IllegalArgumentException("Sender not found") }

        val chatMessage = ChatMessage(
            room = room,
            sender = sender,
            message = createChatMessageDto.message
        )
        val saved = chatMessageRepository.save(chatMessage)

        return ChatMessageResponse(
            id = saved.id,
            roomId = saved.room.id,
            senderId = saved.sender.id!!,
            message = saved.message,
            sentAt = saved.sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    @Transactional(readOnly = true)
    fun getMessages(roomId: Long): List<ChatMessageResponse> {
        val messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId)
        return messages.map {
            ChatMessageResponse(
                id = it.id,
                roomId = it.room.id,
                senderId = it.sender.id!!,
                message = it.message,
                sentAt = it.sentAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }
}