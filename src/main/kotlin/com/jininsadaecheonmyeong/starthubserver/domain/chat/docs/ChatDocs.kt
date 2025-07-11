package com.jininsadaecheonmyeong.starthubserver.domain.chat.docs

import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatMessageResponse
import com.jininsadaecheonmyeong.starthubserver.domain.chat.data.ChatRoomResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Tag(name = "채팅", description = "채팅 관련 API")
interface ChatDocs {
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성하거나 기존 채팅방을 가져옵니다.")
    fun createOrGetRoom(
        @RequestParam companyId: Long,
        @RequestParam userId: UUID,
    ): ResponseEntity<BaseResponse<ChatRoomResponse>>

    @Operation(summary = "채팅 조회", description = "채팅 내역을 가져옵니다.")
    fun getMessages(
        @RequestParam roomId: Long,
    ): ResponseEntity<BaseResponse<List<ChatMessageResponse>>>
}
