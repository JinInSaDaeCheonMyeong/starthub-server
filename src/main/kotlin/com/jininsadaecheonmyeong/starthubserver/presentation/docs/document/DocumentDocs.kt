package com.jininsadaecheonmyeong.starthubserver.presentation.docs.document

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AIEditRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AnswerQuestionsRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.CreateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.SaveAnswersRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.UpdateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentEditHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentListResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentQuestionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Document", description = "AI 문서화 관련 API")
interface DocumentDocs {
    @Operation(summary = "문서 생성", description = "문서 유형을 선택하여 새로운 문서를 생성합니다.")
    fun createDocument(
        @Valid @RequestBody request: CreateDocumentRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>>

    @Operation(summary = "문서 상세 조회", description = "생성된 문서의 상세 정보를 조회합니다.")
    fun getDocument(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<DocumentResponse>>

    @Operation(summary = "내 문서 목록 조회", description = "사용자가 생성한 모든 문서 목록을 조회합니다.")
    fun getMyDocuments(): ResponseEntity<BaseResponse<List<DocumentListResponse>>>

    @Operation(summary = "질문 목록 조회", description = "문서 생성을 위한 질문 목록을 조회합니다.")
    fun getQuestions(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<List<DocumentQuestionResponse>>>

    @Operation(summary = "질문 답변 저장", description = "질문에 대한 답변을 저장합니다. (문서 생성 없이 답변만 저장)")
    fun saveAnswers(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: SaveAnswersRequest,
    ): ResponseEntity<BaseResponse<List<DocumentQuestionResponse>>>

    @Operation(summary = "템플릿 파일 업로드", description = "지원 사업 계획서 양식 파일을 업로드하면 파일 기반 질문을 생성합니다. PDF, DOCX, HWP 형식 지원 (최대 20MB)")
    fun uploadTemplate(
        @PathVariable documentId: Long,
        file: MultipartFile,
    ): ResponseEntity<BaseResponse<List<DocumentQuestionResponse>>>

    @Operation(summary = "문서 생성 (AI)", description = "질문 답변을 제출하고 AI가 문서를 생성합니다.")
    fun generateDocument(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AnswerQuestionsRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>>

    @Operation(summary = "문서 수정", description = "문서 제목이나 내용을 직접 수정합니다. (자동 저장)")
    fun updateDocument(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: UpdateDocumentRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>>

    @Operation(summary = "AI 부분 수정", description = "AI에게 문서의 특정 부분 수정을 요청합니다.")
    fun aiEdit(
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AIEditRequest,
    ): ResponseEntity<BaseResponse<DocumentResponse>>

    @Operation(summary = "작업 히스토리 조회", description = "문서의 작업 히스토리를 조회합니다.")
    fun getEditHistory(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<List<DocumentEditHistoryResponse>>>

    @Operation(summary = "문서 삭제", description = "문서를 삭제합니다. (Soft Delete)")
    fun deleteDocument(
        @PathVariable documentId: Long,
    ): ResponseEntity<BaseResponse<Unit>>
}
