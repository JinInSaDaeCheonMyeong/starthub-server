package com.jininsadaecheonmyeong.starthubserver.presentation.docs.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.CreateSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.UpdateSessionTitleRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux

@Tag(name = "챗봇", description = "AI 챗봇 API")
interface AIChatbotDocs {
    @Operation(summary = "채팅 세션 생성", description = "새로운 AI 채팅 세션을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "세션 생성 성공",
                content = [Content(schema = Schema(implementation = ChatSessionResponse::class))],
            ),
        ],
    )
    fun createSession(request: CreateSessionRequest): ResponseEntity<BaseResponse<ChatSessionResponse>>

    @Operation(summary = "채팅 세션 목록 조회", description = "현재 사용자의 모든 채팅 세션을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "세션 목록 조회 성공",
            ),
        ],
    )
    fun getSessions(): ResponseEntity<BaseResponse<List<ChatSessionResponse>>>

    @Operation(summary = "채팅 세션 상세 조회", description = "특정 채팅 세션의 상세 정보와 메시지 히스토리를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "세션 조회 성공",
                content = [Content(schema = Schema(implementation = ChatSessionDetailResponse::class))],
            ),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun getSession(
        @Parameter(description = "세션 ID") sessionId: Long,
    ): ResponseEntity<BaseResponse<ChatSessionDetailResponse>>

    @Operation(summary = "채팅 세션 제목 수정", description = "채팅 세션의 제목을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "제목 수정 성공"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun updateSessionTitle(
        @Parameter(description = "세션 ID") sessionId: Long,
        request: UpdateSessionTitleRequest,
    ): ResponseEntity<BaseResponse<ChatSessionResponse>>

    @Operation(summary = "채팅 세션 삭제", description = "채팅 세션을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "세션 삭제 성공"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun deleteSession(
        @Parameter(description = "세션 ID") sessionId: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "메시지 전송 (스트리밍)",
        description = """
            SSE(Server-Sent Events) 방식으로 응답합니다. Swagger에서 테스트 불가하며, curl이나 fetch API로 테스트하세요.
            파일 첨부 시 form-data로 전송하며, 지원 형식: PDF, DOCX, 이미지(PNG, JPG, GIF, WEBP)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스트리밍 응답 시작 (Content-Type: text/event-stream)",
            ),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun sendMessageStream(
        @Parameter(description = "세션 ID", example = "1") sessionId: Long,
        @Parameter(description = "전송할 메시지") message: String,
        @Parameter(description = "첨부 파일 목록 (선택)") files: List<MultipartFile>?,
    ): Flux<ServerSentEvent<String>>
}
