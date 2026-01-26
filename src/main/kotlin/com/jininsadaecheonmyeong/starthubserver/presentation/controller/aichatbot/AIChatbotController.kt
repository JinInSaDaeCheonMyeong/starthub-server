package com.jininsadaecheonmyeong.starthubserver.presentation.controller.aichatbot

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.FileProcessingService
import com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot.AIChatbotUseCase
import com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot.ProcessedFile
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.storage.GcsStorageService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.aichatbot.AIChatbotDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.CreateSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.UpdateSessionTitleRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.StreamChunkResponse
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.flux
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/chatbot")
class AIChatbotController(
    private val aiChatbotUseCase: AIChatbotUseCase,
    private val fileProcessingService: FileProcessingService,
    private val gcsStorageService: GcsStorageService,
    private val objectMapper: ObjectMapper,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : AIChatbotDocs {
    @PostMapping("/sessions")
    override fun createSession(
        @RequestBody request: CreateSessionRequest,
    ): ResponseEntity<BaseResponse<ChatSessionResponse>> {
        val session = aiChatbotUseCase.createSession(request.title)
        return BaseResponse.of(ChatSessionResponse.from(session), "세션이 생성되었습니다.")
    }

    @GetMapping("/sessions")
    override fun getSessions(): ResponseEntity<BaseResponse<List<ChatSessionResponse>>> {
        val sessions = aiChatbotUseCase.getSessions()
        return BaseResponse.of(sessions, "세션 목록을 조회했습니다.")
    }

    @GetMapping("/sessions/{sessionId}")
    override fun getSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<ChatSessionDetailResponse>> {
        val session = aiChatbotUseCase.getSessionWithMessages(sessionId)
        return BaseResponse.of(ChatSessionDetailResponse.from(session), "세션을 조회했습니다.")
    }

    @PatchMapping("/sessions/{sessionId}")
    override fun updateSessionTitle(
        @PathVariable sessionId: Long,
        @Valid @RequestBody request: UpdateSessionTitleRequest,
    ): ResponseEntity<BaseResponse<ChatSessionResponse>> {
        val session = aiChatbotUseCase.updateSessionTitle(sessionId, request.title)
        return BaseResponse.of(ChatSessionResponse.from(session), "세션 제목이 수정되었습니다.")
    }

    @DeleteMapping("/sessions/{sessionId}")
    override fun deleteSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        aiChatbotUseCase.deleteSession(sessionId)
        return BaseResponse.of(Unit, "세션이 삭제되었습니다.")
    }

    @PostMapping(
        "/sessions/{sessionId}/messages/stream",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    override fun sendMessageStream(
        @PathVariable sessionId: Long,
        @RequestPart("message") message: String,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): Flux<ServerSentEvent<String>> {
        val user = userAuthenticationHolder.current()

        val processedFiles = files?.mapNotNull { file ->
            val fileName = file.originalFilename ?: "unknown"
            val fileExtension = fileName.substringAfterLast(".", "").lowercase()

            val supportedTypes = listOf("pdf", "docx", "doc", "hwp", "png", "jpg", "jpeg", "gif", "webp")
            if (fileExtension !in supportedTypes) {
                return@mapNotNull null
            }

            val fileUrl = gcsStorageService.uploadFile(file, "chatbot-documents")

            val extractedText = when (fileExtension) {
                "pdf" -> fileProcessingService.extractTextFromPdf(file)
                "docx", "doc" -> fileProcessingService.extractTextFromDocx(file)
                "hwp" -> fileProcessingService.extractTextFromHwp(file)
                else -> null
            }

            ProcessedFile(
                fileName = fileName,
                fileUrl = fileUrl,
                fileType = fileExtension,
                extractedText = extractedText,
            )
        }

        return flux {
            aiChatbotUseCase.processMessageStream(sessionId, message, user, processedFiles)
                .collect { chunk ->
                    val response = StreamChunkResponse.from(chunk)
                    send(
                        ServerSentEvent.builder<String>()
                            .event(response.type.lowercase())
                            .data(objectMapper.writeValueAsString(response))
                            .build(),
                    )
                }
        }
    }
}
