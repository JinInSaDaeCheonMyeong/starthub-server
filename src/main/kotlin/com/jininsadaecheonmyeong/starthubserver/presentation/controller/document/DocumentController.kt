package com.jininsadaecheonmyeong.starthubserver.presentation.controller.document

import com.jininsadaecheonmyeong.starthubserver.application.usecase.document.DocumentUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.document.DocumentDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AIEditRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AnswerQuestionsRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.CreateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.UpdateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentEditHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentListResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentQuestionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/documents")
class DocumentController(
    private val documentUseCase: DocumentUseCase,
) : DocumentDocs {
    @PostMapping
    override fun createDocument(
        @Valid @RequestBody request: CreateDocumentRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>> {
        val response = documentUseCase.createDocument(request)
        return BaseResponse.of(response, "문서 생성 성공")
    }

    @GetMapping("/{documentId}")
    override fun getDocument(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<DocumentResponse>> {
        val response = documentUseCase.getDocument(documentId)
        return BaseResponse.of(response, "문서 조회 성공")
    }

    @GetMapping
    override fun getMyDocuments(): ResponseEntity<BaseResponse<List<DocumentListResponse>>> {
        val response = documentUseCase.getMyDocuments()
        return BaseResponse.of(response, "문서 목록 조회 성공")
    }

    @GetMapping("/{documentId}/questions")
    override fun getQuestions(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<List<DocumentQuestionResponse>>> {
        val response = documentUseCase.getQuestions(documentId)
        return BaseResponse.of(response, "질문 목록 조회 성공")
    }

    @PostMapping("/{documentId}/upload-template")
    override fun uploadTemplate(
        @PathVariable documentId: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<BaseResponse<List<DocumentQuestionResponse>>> {
        val response = documentUseCase.uploadTemplate(documentId, file)
        return BaseResponse.of(response, "템플릿 업로드 성공")
    }

    @PostMapping("/{documentId}/generate")
    override fun generateDocument(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AnswerQuestionsRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>> {
        val response = documentUseCase.generateDocument(documentId, request)
        return BaseResponse.of(response, "문서 생성 성공")
    }

    @PutMapping("/{documentId}")
    override fun updateDocument(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: UpdateDocumentRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>> {
        val response = documentUseCase.updateDocument(documentId, request)
        return BaseResponse.of(response, "문서 수정 성공")
    }

    @PostMapping("/{documentId}/ai-edit")
    override fun aiEdit(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AIEditRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>> {
        val response = documentUseCase.aiEdit(documentId, request)
        return BaseResponse.of(response, "AI 수정 성공")
    }

    @GetMapping("/{documentId}/history")
    override fun getEditHistory(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<List<DocumentEditHistoryResponse>>> {
        val response = documentUseCase.getEditHistory(documentId)
        return BaseResponse.of(response, "히스토리 조회 성공")
    }

    @DeleteMapping("/{documentId}")
    override fun deleteDocument(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        documentUseCase.deleteDocument(documentId)
        return BaseResponse.of(Unit, "문서 삭제 성공")
    }
}