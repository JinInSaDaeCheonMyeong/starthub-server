package com.jininsadaecheonmyeong.starthubserver.application.usecase.document

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.FileProcessingService
import com.jininsadaecheonmyeong.starthubserver.application.service.document.DocumentAIService
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentEditHistory
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.DocumentTemplate
import com.jininsadaecheonmyeong.starthubserver.domain.entity.document.GeneratedDocument
import com.jininsadaecheonmyeong.starthubserver.domain.enums.document.DocumentType
import com.jininsadaecheonmyeong.starthubserver.domain.exception.document.DocumentAccessDeniedException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.document.DocumentNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.document.DocumentEditHistoryRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.document.DocumentQuestionRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.document.DocumentTemplateRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.document.GeneratedDocumentRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.storage.GcsStorageService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AIEditRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.AnswerQuestionsRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.CreateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document.UpdateDocumentRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentEditHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentListResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentQuestionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.document.DocumentResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Component
@Transactional(readOnly = true)
class DocumentUseCase(
    private val documentRepository: GeneratedDocumentRepository,
    private val questionRepository: DocumentQuestionRepository,
    private val templateRepository: DocumentTemplateRepository,
    private val editHistoryRepository: DocumentEditHistoryRepository,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val documentAIService: DocumentAIService,
    private val fileProcessingService: FileProcessingService,
    private val gcsStorageService: GcsStorageService,
) {

    @Transactional
    fun createDocument(request: CreateDocumentRequest): DocumentResponse {
        val user = userAuthenticationHolder.current()

        val document = GeneratedDocument(
            user = user,
            title = request.title,
            documentType = request.documentType,
        )
        val savedDocument = documentRepository.save(document)

        val questions = generateDefaultQuestions(savedDocument)
        questionRepository.saveAll(questions)

        addHistory(savedDocument, "문서가 생성되었습니다.")

        return DocumentResponse.from(savedDocument)
    }

    fun getDocument(documentId: Long): DocumentResponse {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)
        return DocumentResponse.from(document)
    }

    fun getMyDocuments(): List<DocumentListResponse> {
        val user = userAuthenticationHolder.current()
        return documentRepository.findAllByUserAndDeletedFalseOrderByCreatedAtDesc(user)
            .map { DocumentListResponse.from(it) }
    }

    fun getQuestions(documentId: Long): List<DocumentQuestionResponse> {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)
        return questionRepository.findAllByDocumentOrderByOrderIndexAsc(document)
            .map { DocumentQuestionResponse.from(it) }
    }

    @Transactional
    fun uploadTemplate(
        documentId: Long,
        file: MultipartFile,
    ): List<DocumentQuestionResponse> {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)

        val fileName = file.originalFilename ?: "template"
        val fileType = fileName.substringAfterLast(".", "").lowercase()

        val fileUrl = gcsStorageService.uploadFile(file, "document-templates")

        val extractedText = fileProcessingService.extractTextFromFile(file, fileType)

        val template = DocumentTemplate(
            document = document,
            fileName = fileName,
            fileUrl = fileUrl,
            fileType = fileType,
            extractedText = extractedText,
        )
        templateRepository.save(template)

        questionRepository.deleteAll(
            questionRepository.findAllByDocumentOrderByOrderIndexAsc(document),
        )

        val questions = generateQuestionsFromTemplate(document, extractedText)
        questionRepository.saveAll(questions)

        addHistory(document, "템플릿 파일이 업로드되었습니다: $fileName")

        return questions.map { DocumentQuestionResponse.from(it) }
    }

    @Transactional
    fun generateDocument(
        documentId: Long,
        request: AnswerQuestionsRequest,
    ): DocumentResponse {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)

        document.toneType = request.toneType
        document.markAsGenerating()

        val questions = questionRepository.findAllByDocumentOrderByOrderIndexAsc(document)
        request.answers.forEach { answer ->
            questions.find { it.id == answer.questionId }?.updateAnswer(answer.answer)
        }
        questionRepository.saveAll(questions)

        val content = callAIForGeneration(document, questions)

        document.updateContent(content)
        document.markAsCompleted()
        documentRepository.save(document)

        addHistory(document, "AI가 문서 초안을 생성했습니다.")

        return DocumentResponse.from(document)
    }

    @Transactional
    fun updateDocument(
        documentId: Long,
        request: UpdateDocumentRequest,
    ): DocumentResponse {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)

        request.title?.let { document.title = it }
        request.content?.let { document.updateContent(it) }
        documentRepository.save(document)

        return DocumentResponse.from(document)
    }

    @Transactional
    fun aiEdit(
        documentId: Long,
        request: AIEditRequest,
    ): DocumentResponse {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)

        val editedContent = callAIForEdit(document, request.prompt)

        document.updateContent(editedContent)
        documentRepository.save(document)

        addHistory(document, "AI가 문서를 수정했습니다: ${request.prompt.take(50)}")

        return DocumentResponse.from(document)
    }

    fun getEditHistory(documentId: Long): List<DocumentEditHistoryResponse> {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)
        return editHistoryRepository.findAllByDocumentOrderByCreatedAtDesc(document)
            .map { DocumentEditHistoryResponse.from(it) }
    }

    @Transactional
    fun deleteDocument(documentId: Long) {
        val document = findDocumentOrThrow(documentId)
        validateOwner(document)
        document.delete()
        documentRepository.save(document)
    }

    private fun findDocumentOrThrow(documentId: Long): GeneratedDocument {
        return documentRepository.findByIdAndDeletedFalse(documentId)
            .orElseThrow { DocumentNotFoundException("문서를 찾을 수 없습니다.") }
    }

    private fun validateOwner(document: GeneratedDocument) {
        val user = userAuthenticationHolder.current()
        if (!document.isOwner(user)) {
            throw DocumentAccessDeniedException("문서에 대한 접근 권한이 없습니다.")
        }
    }

    private fun addHistory(document: GeneratedDocument, description: String) {
        editHistoryRepository.save(
            DocumentEditHistory(document = document, description = description),
        )
    }

    private fun generateDefaultQuestions(document: GeneratedDocument): List<DocumentQuestion> {
        val baseQuestions = listOf(
            "해당 문제는 누구(타겟 사용자)에게 발생하나요?" to true,
            "사용자가 겪는 가장 큰 불편은 무엇인가요?" to true,
            "이 문제를 어떻게 해결하려고 하나요?" to true,
        )

        val typeSpecificQuestions = when (document.documentType) {
            DocumentType.PLAN -> listOf(
                "예상되는 수익 모델은 무엇인가요?" to true,
                "경쟁사 대비 차별점은 무엇인가요?" to false,
            )
            DocumentType.PROPOSAL -> listOf(
                "제안의 핵심 가치는 무엇인가요?" to true,
                "기대되는 성과 지표는 무엇인가요?" to false,
            )
            DocumentType.OTHER -> listOf(
                "문서의 주요 목적은 무엇인가요?" to true,
            )
        }

        return (baseQuestions + typeSpecificQuestions).mapIndexed { index, (text, required) ->
            DocumentQuestion(
                document = document,
                questionText = text,
                orderIndex = index + 1,
                required = required,
            )
        }
    }

    private fun generateQuestionsFromTemplate(
        document: GeneratedDocument,
        extractedText: String?,
    ): List<DocumentQuestion> {
        if (extractedText.isNullOrBlank()) {
            return generateDefaultQuestions(document)
        }

        val prompt = buildString {
            appendLine("다음은 사업 계획서/제안서 템플릿에서 추출한 텍스트입니다.")
            appendLine("이 템플릿의 각 섹션을 채우기 위해 사용자에게 물어봐야 할 질문을 생성해주세요.")
            appendLine()
            appendLine("템플릿 내용:")
            appendLine(extractedText.take(3000))
            appendLine()
            appendLine("규칙:")
            appendLine("- 3~7개의 질문을 생성하세요")
            appendLine("- 각 질문은 한 줄로, 번호를 붙여주세요 (1. 2. 3. ...)")
            appendLine("- 질문만 출력하고 다른 설명은 제외하세요")
            appendLine("- 한국어로 작성하세요")
        }

        val aiResponse = documentAIService.chat(
            systemPrompt = "당신은 사업 문서 작성을 돕는 전문 컨설턴트입니다.",
            userMessage = prompt,
        )

        val questions = aiResponse.lines()
            .filter { it.isNotBlank() && it.matches(Regex("^\\d+\\..*")) }
            .map { it.replace(Regex("^\\d+\\.\\s*"), "").trim() }
            .filter { it.isNotBlank() }

        if (questions.isEmpty()) {
            return generateDefaultQuestions(document)
        }

        return questions.mapIndexed { index, questionText ->
            DocumentQuestion(
                document = document,
                questionText = questionText,
                orderIndex = index + 1,
                required = index < 3,
            )
        }
    }

    private fun callAIForGeneration(
        document: GeneratedDocument,
        questions: List<DocumentQuestion>,
    ): String {
        val user = userAuthenticationHolder.current()
        val bmcs = businessModelCanvasRepository.findTop3ByUserAndDeletedFalseOrderByCreatedAtDesc(user)

        val template = templateRepository.findByDocument(document)

        val prompt = buildString {
            appendLine("다음 정보를 바탕으로 ${document.documentType.toKorean()}을(를) 작성해주세요.")
            appendLine()

            appendLine("## 질문 및 답변")
            questions.forEach { q ->
                if (!q.answerText.isNullOrBlank()) {
                    appendLine("Q: ${q.questionText}")
                    appendLine("A: ${q.answerText}")
                    appendLine()
                }
            }

            if (bmcs.isNotEmpty()) {
                appendLine("## 사용자의 BMC 정보")
                bmcs.first().let { bmc ->
                    bmc.customerSegments?.let { appendLine("- 고객 세그먼트: $it") }
                    bmc.valueProposition?.let { appendLine("- 가치 제안: $it") }
                    bmc.channels?.let { appendLine("- 채널: $it") }
                    bmc.customerRelationships?.let { appendLine("- 고객 관계: $it") }
                    bmc.revenueStreams?.let { appendLine("- 수익원: $it") }
                    bmc.keyResources?.let { appendLine("- 핵심 자원: $it") }
                    bmc.keyActivities?.let { appendLine("- 핵심 활동: $it") }
                    bmc.keyPartners?.let { appendLine("- 핵심 파트너: $it") }
                    bmc.costStructure?.let { appendLine("- 비용 구조: $it") }
                }
                appendLine()
            }

            val templateText = template?.extractedText
            if (templateText != null) {
                appendLine("## 참고 템플릿")
                appendLine(templateText.take(3000))
                appendLine()
            }

            appendLine("## 작성 요구사항")
            appendLine("- 톤앤매너: ${document.toneType?.toKorean() ?: "전문적인 & 신뢰감 있는"}")
            appendLine("- 한국어로 작성")
            appendLine("- HTML 형식으로 작성 (h1, h2, p, ul, li, blockquote 태그 사용)")
            appendLine("- 구체적이고 실질적인 내용으로 작성")
        }

        return documentAIService.chat(
            systemPrompt = "당신은 스타트업 사업 문서 작성 전문가입니다. 주어진 정보를 바탕으로 체계적이고 설득력 있는 문서를 작성합니다.",
            userMessage = prompt,
        )
    }

    private fun callAIForEdit(
        document: GeneratedDocument,
        editPrompt: String,
    ): String {
        val prompt = buildString {
            appendLine("다음 문서를 수정 요청에 따라 수정해주세요.")
            appendLine()
            appendLine("## 현재 문서 내용")
            appendLine(document.content)
            appendLine()
            appendLine("## 수정 요청")
            appendLine(editPrompt)
            appendLine()
            appendLine("## 규칙")
            appendLine("- 수정 요청에 해당하는 부분만 수정하고, 나머지는 그대로 유지하세요")
            appendLine("- HTML 형식을 유지하세요")
            appendLine("- 수정된 전체 문서를 반환하세요")
        }

        return documentAIService.chat(
            systemPrompt = "당신은 문서 편집 전문가입니다. 요청에 따라 문서의 특정 부분을 수정합니다.",
            userMessage = prompt,
        )
    }

    private fun DocumentType.toKorean(): String = when (this) {
        DocumentType.PLAN -> "사업 계획서"
        DocumentType.PROPOSAL -> "사업 제안서"
        DocumentType.OTHER -> "문서"
    }

    private fun com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType.toKorean(): String = when (this) {
        com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType.PROFESSIONAL -> "전문적인 & 신뢰감 있는"
        com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType.PERSUASIVE -> "강렬한 & 설득력 있는"
        com.jininsadaecheonmyeong.starthubserver.domain.enums.document.ToneType.SIMPLE -> "쉬운 설명"
    }
}