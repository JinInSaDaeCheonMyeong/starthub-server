package com.jininsadaecheonmyeong.starthubserver.presentation.docs.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.CreateSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.SendMessageRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.UpdateSessionTitleRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatDocumentResponse
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
        description = "AI에게 메시지를 보내고 Server-Sent Events로 스트리밍 응답을 받습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스트리밍 응답 시작"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun sendMessageStream(
        @Parameter(description = "세션 ID") sessionId: Long,
        request: SendMessageRequest,
    ): Flux<ServerSentEvent<String>>

    @Operation(summary = "파일 업로드", description = "채팅 세션에 파일을 업로드합니다. (PDF, DOCX, 이미지 지원)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 성공",
                content = [Content(schema = Schema(implementation = ChatDocumentResponse::class))],
            ),
            ApiResponse(responseCode = "400", description = "지원하지 않는 파일 형식"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun uploadFile(
        @Parameter(description = "세션 ID") sessionId: Long,
        @Parameter(description = "업로드할 파일") file: MultipartFile,
    ): ResponseEntity<BaseResponse<ChatDocumentResponse>>

    @Operation(summary = "파일 목록 조회", description = "채팅 세션에 업로드된 파일 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "파일 목록 조회 성공"),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        ],
    )
    fun getFiles(
        @Parameter(description = "세션 ID") sessionId: Long,
    ): ResponseEntity<BaseResponse<List<ChatDocumentResponse>>>

    @Operation(summary = "파일 삭제", description = "채팅 세션에서 파일을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
            ApiResponse(responseCode = "404", description = "세션 또는 파일을 찾을 수 없음"),
        ],
    )
    fun deleteFile(
        @Parameter(description = "세션 ID") sessionId: Long,
        @Parameter(description = "파일 ID") fileId: Long,
    ): ResponseEntity<BaseResponse<Unit>>
}
