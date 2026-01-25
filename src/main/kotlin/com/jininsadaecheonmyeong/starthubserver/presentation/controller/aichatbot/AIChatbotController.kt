package com.jininsadaecheonmyeong.starthubserver.presentation.controller.aichatbot

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.FileProcessingService
import com.jininsadaecheonmyeong.starthubserver.application.usecase.aichatbot.AIChatbotUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.infra.storage.GcsStorageService
import com.jininsadaecheonmyeong.starthubserver.logger
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.aichatbot.AIChatbotDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.CreateSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.SendMessageRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot.UpdateSessionTitleRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatDocumentResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.ChatSessionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot.StreamChunkResponse
import jakarta.validation.Valid
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
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
import org.springframework.web.bind.annotation.RequestParam
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
) : AIChatbotDocs {
    private val log = logger()

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
        return BaseResponse.of(ChatSessionResponse.fromList(sessions), "세션 목록을 조회했습니다.")
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
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    override fun sendMessageStream(
        @PathVariable sessionId: Long,
        @Valid @RequestBody request: SendMessageRequest,
    ): Flux<ServerSentEvent<String>> {
        log.info("스트리밍 메시지 요청: sessionId=$sessionId, message=${request.message.take(50)}...")

        return runBlocking {
            aiChatbotUseCase.processMessageStream(sessionId, request.message)
                .map { chunk ->
                    val response = StreamChunkResponse.from(chunk)
                    ServerSentEvent.builder<String>()
                        .event(response.type.lowercase())
                        .data(objectMapper.writeValueAsString(response))
                        .build()
                }
                .asFlux()
        }
    }

    @PostMapping("/sessions/{sessionId}/files")
    override fun uploadFile(
        @PathVariable sessionId: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<BaseResponse<ChatDocumentResponse>> {
        val fileName = file.originalFilename ?: "unknown"
        val fileExtension = fileName.substringAfterLast(".", "").lowercase()

        val supportedTypes = listOf("pdf", "docx", "doc", "png", "jpg", "jpeg", "gif", "webp")
        if (fileExtension !in supportedTypes) {
            return BaseResponse.of(null, HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. (지원: PDF, DOCX, 이미지)")
        }

        val fileUrl = gcsStorageService.uploadFile(file, "chatbot-documents")

        val extractedText =
            when (fileExtension) {
                "pdf" -> fileProcessingService.extractTextFromPdf(file)
                "docx", "doc" -> fileProcessingService.extractTextFromDocx(file)
                else -> null
            }

        val document =
            runBlocking {
                aiChatbotUseCase.addDocument(
                    sessionId = sessionId,
                    fileName = fileName,
                    fileUrl = fileUrl,
                    fileType = fileExtension,
                    extractedText = extractedText,
                )
            }

        return BaseResponse.of(ChatDocumentResponse.from(document), "파일이 업로드되었습니다.")
    }

    @GetMapping("/sessions/{sessionId}/files")
    override fun getFiles(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<List<ChatDocumentResponse>>> {
        val documents = aiChatbotUseCase.getDocuments(sessionId)
        return BaseResponse.of(documents.map { ChatDocumentResponse.from(it) }, "파일 목록을 조회했습니다.")
    }

    @DeleteMapping("/sessions/{sessionId}/files/{fileId}")
    override fun deleteFile(
        @PathVariable sessionId: Long,
        @PathVariable fileId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        runBlocking {
            aiChatbotUseCase.deleteDocument(sessionId, fileId)
        }
        return BaseResponse.of(Unit, "파일이 삭제되었습니다.")
    }
}
