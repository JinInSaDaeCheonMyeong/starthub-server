package com.jininsadaecheonmyeong.starthubserver.presentation.docs.chat

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.chat.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.chat.ChatRoomResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "채팅", description = "채팅 관련 API")
interface ChatDocs {
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성하거나 기존 채팅방을 가져옵니다.")
    fun createOrGetRoom(
        @RequestParam userId: Long,
        @RequestParam companyId: Long,
    ): ResponseEntity<BaseResponse<ChatRoomResponse>>

    @Operation(summary = "채팅 조회", description = "채팅 내역을 가져옵니다.")
    fun getMessages(
        @RequestParam roomId: Long,
    ): ResponseEntity<BaseResponse<List<ChatMessageResponse>>>

    @Operation(summary = "나의 채팅방 목록 조회", description = "현재 로그인한 사용자가 속한 모든 채팅방 목록을 조회합니다.")
    fun getUserChatRooms(): ResponseEntity<BaseResponse<List<ChatRoomResponse>>>
}
