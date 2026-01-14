package com.jininsadaecheonmyeong.starthubserver.service.bmc

import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.BmcModificationType
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.UpdateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.UpdateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcFormResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.event.bmc.BmcCreatedEvent
import com.jininsadaecheonmyeong.starthubserver.exception.bmc.BmcNotFoundException
import com.jininsadaecheonmyeong.starthubserver.exception.bmc.BmcSessionNotCompletedException
import com.jininsadaecheonmyeong.starthubserver.exception.bmc.BmcSessionNotFoundException
import com.jininsadaecheonmyeong.starthubserver.global.infra.storage.GcsStorageService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.logger
import com.jininsadaecheonmyeong.starthubserver.repository.bmc.BmcModificationRequestRepository
import com.jininsadaecheonmyeong.starthubserver.repository.bmc.BmcQuestionRepository
import com.jininsadaecheonmyeong.starthubserver.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.usecase.bmc.AnonymousBmcData
import com.jininsadaecheonmyeong.starthubserver.usecase.bmc.BmcUseCase
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class BmcService(
    private val chatModel: ChatModel,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val bmcQuestionRepository: BmcQuestionRepository,
    private val bmcModificationRequestRepository: BmcModificationRequestRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val gcsStorageService: GcsStorageService,
    private val eventPublisher: ApplicationEventPublisher,
) : BmcUseCase {
    private val log = logger()

    // ==================== Session Management (BmcQuestionService) ====================

    override fun createBmcSession(request: CreateBmcSessionRequest): Pair<BmcSessionResponse, String> {
        val user = userAuthenticationHolder.current()

        val existingSession = bmcQuestionRepository.findByUserAndTitleAndIsCompletedFalse(user, request.title)
        if (existingSession.isPresent) {
            return BmcSessionResponse.from(existingSession.get()) to "동일한 제목의 미완료 세션이 존재하여 기존 세션을 반환합니다."
        }

        val bmcQuestion =
            BmcQuestion(
                user = user,
                title = request.title,
                templateType = request.templateType,
            )

        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion) to "BMC 세션 생성 성공"
    }

    override fun answerQuestion(request: AnswerQuestionRequest): BmcSessionResponse {
        val user = userAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(request.sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        bmcQuestion.updateAnswer(request.questionNumber, request.answer)
        if (isSessionCompleted(bmcQuestion)) bmcQuestion.markAsCompleted()
        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    @Transactional(readOnly = true)
    override fun getBmcSession(sessionId: Long): BmcSessionResponse {
        val user = userAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        return BmcSessionResponse.from(bmcQuestion)
    }

    @Transactional(readOnly = true)
    override fun getAllBmcSessions(): List<BmcSessionResponse> {
        val user = userAuthenticationHolder.current()
        val bmcQuestions = bmcQuestionRepository.findAllByUserOrderByCreatedAtDesc(user)

        return bmcQuestions.map { BmcSessionResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getBmcQuestions(): List<BmcFormResponse> {
        val questions =
            listOf(
                "사업 아이디어는 무엇인가요?\n" +
                    "제품이나 서비스를 간단하게 설명해주세요.",
                "우리의 핵심 고객은 누구인가요?\n" +
                    "우리가 해결해 주고자 하는 고객의 가장 큰 문제 또는 욕구는 무엇인가요?",
                "우리는 어떤 핵심 가치를 제공하나요?\n" +
                    "고객의 문제를 해결하기 위해 우리가 제공하는 핵심적인 가치는 무엇인가요?",
                "고객은 우리를 어떻게 만나고 경험하나요?\n" +
                    "어떤 경로를 통해 우리 서비스를 이용하고, 구매를 결정하게 되나요?",
                "고객과 어떻게 관계를 맺고 유지하나요?\n" +
                    "고객이 우리 서비스에 만족하고 계속 사용하게 하려면 어떤 관계를 맺어야 할까요?",
                "수익은 어떻게 창출되나요?\n" +
                    "구체적으로 어떤 방식으로 수익이 발생하게 되나요?",
                "우리의 핵심 자원은 무엇인가요?\n" +
                    "이 비즈니스를 운영하는 데 반드시 필요한 기술, 특허, 데이터, 인력, 자금 등은 무엇인가요?",
                "어떤 핵심 활동에 집중해야 하나요?\n" +
                    "제품 개발, 마케팅, 고객 관리 등 우리의 시간과 노력이 가장 많이 투입되어야 하는 일은 무엇인가요?",
                "누구와 협력해야 하나요?\n" +
                    "이 비즈니스를 성공시키기 위해 어떤 파트너와의 협력이 필요한가요?",
                "어떤 비용이 발생하나요?\n" +
                    "서비스를 개발하고 고객에게 가치를 제공하는 과정에서 발생하는 가장 큰 비용은 무엇인가요?",
            )

        return questions.mapIndexed { i, q ->
            BmcFormResponse(
                questionNumber = i.inc(),
                question = q,
            )
        }
    }

    override fun updateSessionAnswers(
        sessionId: Long,
        request: UpdateBmcSessionRequest,
    ): BmcSessionResponse {
        val user = userAuthenticationHolder.current()
        val bmcQuestion =
            bmcQuestionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }

        request.answers.forEach { answerUpdate ->
            bmcQuestion.updateAnswer(answerUpdate.questionNumber, answerUpdate.answer)
        }

        request.templateType?.let { bmcQuestion.templateType = it }

        if (isSessionCompleted(bmcQuestion)) bmcQuestion.markAsCompleted()
        val savedBmcQuestion = bmcQuestionRepository.save(bmcQuestion)
        return BmcSessionResponse.from(savedBmcQuestion)
    }

    private fun isSessionCompleted(bmcQuestion: BmcQuestion): Boolean {
        return bmcQuestion.getAllAnswers().all { it != null && it.isNotBlank() }
    }

    private fun getBmcQuestionEntity(sessionId: Long): BmcQuestion {
        val user = userAuthenticationHolder.current()
        return bmcQuestionRepository.findByIdAndUser(sessionId, user)
            .orElseThrow { BmcSessionNotFoundException("BMC 세션을 찾을 수 없습니다.") }
    }

    // ==================== BMC Generation (BmcGenerationService) ====================

    override fun generateBusinessModelCanvas(request: GenerateBmcRequest): Pair<BusinessModelCanvasResponse, String> {
        val user = userAuthenticationHolder.current()
        val bmcQuestion = getBmcQuestionEntity(request.sessionId)
        if (!bmcQuestion.isCompleted) {
            throw BmcSessionNotCompletedException("모든 질문에 답변을 완료해야 BMC를 생성할 수 있습니다.")
        }

        val existingBmc = businessModelCanvasRepository.findByBmcQuestionAndUserAndDeletedFalse(bmcQuestion, user)
        if (existingBmc.isPresent) {
            return BusinessModelCanvasResponse.from(existingBmc.get()) to "이미 생성된 BMC가 존재하여 기존 BMC를 반환합니다."
        }

        val prompt = generateBmcPrompt(bmcQuestion)

        try {
            val response = chatModel.call(prompt)
            val bmcElements = parseBmcResponse(response)
            val businessModelCanvas = createBusinessModelCanvas(user, bmcQuestion, bmcElements, bmcQuestion)

            val savedBmc = businessModelCanvasRepository.save(businessModelCanvas)
            log.info("BMC 생성 완료: sessionId={}, userId={}, bmcId={}", request.sessionId, user.id, savedBmc.id)

            publishBmcCreatedEvent(savedBmc, user)

            return BusinessModelCanvasResponse.from(savedBmc) to "BMC 생성 성공"
        } catch (e: Exception) {
            log.error("BMC 생성 중 오류 발생: sessionId={}, userId={}, error={}", request.sessionId, user.id, e.message, e)
            throw RuntimeException("BMC 생성 중 오류가 발생했습니다. 다시 시도해주세요.", e)
        }
    }

    override fun updateSessionAnswersAndRegenerate(
        sessionId: Long,
        request: UpdateBmcSessionRequest,
    ): BusinessModelCanvasResponse {
        val user = userAuthenticationHolder.current()
        updateSessionAnswers(sessionId, request)
        val bmcQuestion = getBmcQuestionEntity(sessionId)
        if (!bmcQuestion.isCompleted) {
            throw BmcSessionNotCompletedException("모든 질문에 답변을 완료해야 BMC를 생성할 수 있습니다.")
        }
        val prompt = generateBmcPrompt(bmcQuestion)

        try {
            val response = chatModel.call(prompt)
            val bmcElements = parseBmcResponse(response)
            val businessModelCanvas = createBusinessModelCanvas(user, bmcQuestion, bmcElements, null)
            val savedBmc = businessModelCanvasRepository.save(businessModelCanvas)
            publishBmcCreatedEvent(savedBmc, user)

            return BusinessModelCanvasResponse.from(savedBmc)
        } catch (e: Exception) {
            log.error("BMC 재생성 중 오류 발생: sessionId={}, userId={}, error={}", sessionId, user.id, e.message, e)
            throw RuntimeException("BMC 재생성 중 오류가 발생했습니다. 다시 시도해주세요.", e)
        }
    }

    private fun createBusinessModelCanvas(
        user: User,
        bmcQuestion: BmcQuestion,
        bmcElements: Map<String, String>,
        associatedBmcQuestion: BmcQuestion?,
    ): BusinessModelCanvas {
        return BusinessModelCanvas(
            user = user,
            title = bmcQuestion.title,
            templateType = bmcQuestion.templateType,
            customerSegments = bmcElements["CUSTOMER_SEGMENTS"],
            valueProposition = bmcElements["VALUE_PROPOSITION"],
            channels = bmcElements["CHANNELS"],
            customerRelationships = bmcElements["CUSTOMER_RELATIONSHIPS"],
            revenueStreams = bmcElements["REVENUE_STREAMS"],
            keyResources = bmcElements["KEY_RESOURCES"],
            keyActivities = bmcElements["KEY_ACTIVITIES"],
            keyPartners = bmcElements["KEY_PARTNERS"],
            costStructure = bmcElements["COST_STRUCTURE"],
            isCompleted = true,
            bmcQuestion = associatedBmcQuestion,
        )
    }

    private fun publishBmcCreatedEvent(
        businessModelCanvas: BusinessModelCanvas,
        user: User,
    ) {
        try {
            val event =
                BmcCreatedEvent(
                    source = this,
                    businessModelCanvas = businessModelCanvas,
                    user = user,
                )
            eventPublisher.publishEvent(event)
        } catch (e: Exception) {
            log.error("Failed to publish BmcCreatedEvent for BMC ID: {}", businessModelCanvas.id, e)
        }
    }

    // ==================== BMC Modification (BmcModificationService) ====================

    override fun requestBmcModification(request: ModifyBmcRequest): BmcModificationResponse {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        val modificationRequest =
            BmcModificationRequest(
                user = user,
                businessModelCanvas = bmc,
                modificationRequest = request.modificationRequest,
                requestType = request.requestType,
            )

        val savedRequest = bmcModificationRequestRepository.save(modificationRequest)

        try {
            val prompt =
                when (request.requestType) {
                    BmcModificationType.MODIFY -> generateModificationPrompt(bmc, request.modificationRequest)
                    BmcModificationType.REGENERATE -> generateRegenerationPrompt(bmc, request.modificationRequest)
                }

            val aiResponse = chatModel.call(prompt)
            val bmcElements = parseBmcResponse(aiResponse)

            bmc.updateCanvas(
                customerSegments = bmcElements["CUSTOMER_SEGMENTS"],
                valueProposition = bmcElements["VALUE_PROPOSITION"],
                channels = bmcElements["CHANNELS"],
                customerRelationships = bmcElements["CUSTOMER_RELATIONSHIPS"],
                revenueStreams = bmcElements["REVENUE_STREAMS"],
                keyResources = bmcElements["KEY_RESOURCES"],
                keyActivities = bmcElements["KEY_ACTIVITIES"],
                keyPartners = bmcElements["KEY_PARTNERS"],
                costStructure = bmcElements["COST_STRUCTURE"],
            )

            val updatedBmc = businessModelCanvasRepository.save(bmc)
            savedRequest.markAsProcessed(aiResponse)
            bmcModificationRequestRepository.save(savedRequest)

            log.info("BMC 수정 완료: bmcId={}, userId={}, requestType={}", request.bmcId, user.id, request.requestType)

            return BmcModificationResponse.from(savedRequest, BusinessModelCanvasResponse.from(updatedBmc))
        } catch (e: Exception) {
            log.error("BMC 수정 중 오류 발생: bmcId={}, userId={}, error={}", request.bmcId, user.id, e.message, e)
            throw RuntimeException("BMC 수정 중 오류가 발생했습니다. 다시 시도해주세요.", e)
        }
    }

    @Transactional(readOnly = true)
    override fun getBmcModificationHistory(bmcId: Long): List<BmcModificationResponse> {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        val modifications = bmcModificationRequestRepository.findByBusinessModelCanvasAndUserOrderByCreatedAtDesc(bmc, user)
        return modifications.map { BmcModificationResponse.from(it) }
    }

    // ==================== BMC CRUD Operations (BusinessModelCanvasService) ====================

    @Transactional(readOnly = true)
    override fun getBusinessModelCanvas(id: Long): BusinessModelCanvasResponse {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(id)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        return BusinessModelCanvasResponse.from(bmc)
    }

    @Transactional(readOnly = true)
    override fun getAllBusinessModelCanvases(): List<BusinessModelCanvasResponse> {
        val user = userAuthenticationHolder.current()
        val bmcs = businessModelCanvasRepository.findAllByUserAndDeletedFalse(user)

        return bmcs.map { BusinessModelCanvasResponse.from(it) }
    }

    override fun deleteBusinessModelCanvas(id: Long) {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(id)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        bmc.delete()
        businessModelCanvasRepository.save(bmc)
    }

    override fun updateBusinessModelCanvas(request: UpdateBmcRequest): BusinessModelCanvasResponse {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        bmc.updateCanvas(
            title = request.title,
            templateType = request.templateType,
            customerSegments = request.customerSegments,
            valueProposition = request.valueProposition,
            channels = request.channels,
            customerRelationships = request.customerRelationships,
            revenueStreams = request.revenueStreams,
            keyResources = request.keyResources,
            keyActivities = request.keyActivities,
            keyPartners = request.keyPartners,
            costStructure = request.costStructure,
            imageUrl = request.imageUrl,
        )

        val updatedBmc = businessModelCanvasRepository.save(bmc)
        return BusinessModelCanvasResponse.from(updatedBmc)
    }

    override fun uploadBmcImage(
        bmcId: Long,
        imageFile: MultipartFile,
    ): BusinessModelCanvasResponse {
        val user = userAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        bmc.imageUrl?.let { gcsStorageService.deleteFile(it) }
        val imageUrl = gcsStorageService.uploadFile(imageFile, "bmc-images")
        bmc.updateImageUrl(imageUrl)
        val updatedBmc = businessModelCanvasRepository.save(bmc)
        return BusinessModelCanvasResponse.from(updatedBmc)
    }

    // ==================== Anonymous BMC Operations (AnonymousBmcService) ====================

    @Transactional(readOnly = true)
    override fun getAnonymousCompletedBmcs(limit: Int): List<AnonymousBmcData> {
        return businessModelCanvasRepository.findAllByDeletedFalse()
            .filter { it.isCompleted }
            .shuffled()
            .take(limit)
            .map { bmc ->
                AnonymousBmcData(
                    valueProposition = bmc.valueProposition,
                    customerSegments = bmc.customerSegments,
                    channels = bmc.channels,
                    customerRelationships = bmc.customerRelationships,
                    revenueStreams = bmc.revenueStreams,
                    keyResources = bmc.keyResources,
                    keyActivities = bmc.keyActivities,
                    keyPartners = bmc.keyPartners,
                    costStructure = bmc.costStructure,
                )
            }
    }

    @Transactional(readOnly = true)
    override fun findSimilarBmcs(
        targetBmc: AnonymousBmcData,
        limit: Int,
    ): List<AnonymousBmcData> {
        val allBmcs = getAnonymousCompletedBmcs(100)

        return allBmcs.asSequence()
            .map { bmc ->
                bmc to calculateSimilarity(targetBmc, bmc)
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }

    private fun calculateSimilarity(
        bmc1: AnonymousBmcData,
        bmc2: AnonymousBmcData,
    ): Double {
        val fields =
            listOf(
                bmc1.valueProposition to bmc2.valueProposition,
                bmc1.customerSegments to bmc2.customerSegments,
                bmc1.channels to bmc2.channels,
                bmc1.revenueStreams to bmc2.revenueStreams,
                bmc1.keyActivities to bmc2.keyActivities,
            )

        return fields.mapNotNull { (field1, field2) ->
            if (field1.isNullOrBlank() || field2.isNullOrBlank()) {
                null
            } else {
                calculateTextSimilarity(field1, field2)
            }
        }.average()
    }

    private fun calculateTextSimilarity(
        text1: String,
        text2: String,
    ): Double {
        val words1 = text1.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        val words2 = text2.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return intersection.toDouble() / union.toDouble()
    }

    // ==================== Prompt Generation (BmcPromptService - now private) ====================

    private fun generateBmcPrompt(bmcQuestion: BmcQuestion): String {
        val questions =
            listOf(
                "사업 아이디어는 무엇인가요?\n" +
                    "제품이나 서비스를 간단하게 설명해주세요.",
                "우리의 핵심 고객은 누구인가요?\n" +
                    "우리가 해결해 주고자 하는 고객의 가장 큰 문제 또는 욕구는 무엇인가요?",
                "우리는 어떤 핵심 가치를 제공하나요?\n" +
                    "고객의 문제를 해결하기 위해 우리가 제공하는 핵심적인 가치는 무엇인가요?",
                "고객은 우리를 어떻게 만나고 경험하나요?\n" +
                    "어떤 경로를 통해 우리 서비스를 이용하고, 구매를 결정하게 되나요?",
                "고객과 어떻게 관계를 맺고 유지하나요?\n" +
                    "고객이 우리 서비스에 만족하고 계속 사용하게 하려면 어떤 관계를 맪어야 할까요?",
                "수익은 어떻게 창출되나요?\n" +
                    "구체적으로 어떤 방식으로 수익이 발생하게 되나요?",
                "우리의 핵심 자원은 무엇인가요?\n" +
                    "이 비즈니스를 운영하는 데 반드시 필요한 기술, 특허, 데이터, 인력, 자금 등은 무엇인가요?",
                "어떤 핵심 활동에 집중해야 하나요?\n" +
                    "제품 개발, 마케팅, 고객 관리 등 우리의 시간과 노력이 가장 많이 투입되어야 하는 일은 무엇인가요?",
                "누구와 협력해야 하나요?\n" +
                    "이 비즈니스를 성공시키기 위해 어떤 파트너와의 협력이 필요한가요?",
                "어떤 비용이 발생하나요?\n" +
                    "서비스를 개발하고 고객에게 가치를 제공하는 과정에서 발생하는 가장 큰 비용은 무엇인가요?",
            )

        val answers = bmcQuestion.getAllAnswers()

        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("사용자가 제공한 사업 아이디어와 질문-답변을 바탕으로 BMC의 9가지 요소를 작성해주세요.")
            appendLine()
            appendLine("사업 제목: ${bmcQuestion.title}")
            appendLine()
            appendLine("질문과 답변:")
            questions.forEachIndexed { index, question ->
                val answer = answers[index]
                appendLine("${index + 1}. $question")
                appendLine("답변: ${answer ?: "답변 없음"}")
                appendLine()
            }
            appendLine()
            appendLine("위 정보를 바탕으로 다음 9가지 BMC 요소를 구체적이고 실용적으로 작성해주세요:")
            appendLine()
            appendLine("1. 목표 고객 (Customer Segments): 주요 고객층")
            appendLine("2. 제공 가치 (Value Proposition): 고객에게 제공하는 핵심 가치")
            appendLine("3. 채널 (Channels): 고객에게 가치를 전달하는 경로")
            appendLine("4. 고객 관계 (Customer Relationships): 고객과의 관계 형성 및 유지 방식")
            appendLine("5. 수익 구조 (Revenue Streams): 수익 창출 방식")
            appendLine("6. 핵심 자원 (Key Resources): 사업 운영에 필요한 핵심 자원")
            appendLine("7. 핵심 활동 (Key Activities): 비즈니스 모델이 작동하기 위한 핵심 활동")
            appendLine("8. 핵심 파트너 (Key Partners): 사업 성공을 위한 핵심 파트너와 협력업체")
            appendLine("9. 비용 구조 (Cost Structure): 주요 비용 요소")
            appendLine()
            appendLine("각 요소는 한국어로 작성하고, 구체적이고 실행 가능한 내용으로 작성해주세요.")
            appendLine("각 요소는 2-4개의 핵심 포인트로 구성하고, 불필요한 설명은 제외해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
        }
    }

    private fun generateModificationPrompt(
        bmc: BusinessModelCanvas,
        modificationRequest: String,
    ): String {
        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("기존 BMC를 사용자의 요청에 따라 수정해주세요.")
            appendLine()
            appendLine("현재 BMC:")
            appendLine("제목: ${bmc.title}")
            appendLine("1. 목표 고객: ${bmc.customerSegments ?: "없음"}")
            appendLine("2. 제공 가치: ${bmc.valueProposition ?: "없음"}")
            appendLine("3. 채널: ${bmc.channels ?: "없음"}")
            appendLine("4. 고객 관계: ${bmc.customerRelationships ?: "없음"}")
            appendLine("5. 수익 구조: ${bmc.revenueStreams ?: "없음"}")
            appendLine("6. 핵심 자원: ${bmc.keyResources ?: "없음"}")
            appendLine("7. 핵심 활동: ${bmc.keyActivities ?: "없음"}")
            appendLine("8. 핵심 파트너: ${bmc.keyPartners ?: "없음"}")
            appendLine("9. 비용 구조: ${bmc.costStructure ?: "없음"}")
            appendLine()
            appendLine("수정 요청:")
            appendLine(modificationRequest)
            appendLine()
            appendLine("위 수정 요청을 반영하여 BMC를 업데이트해주세요.")
            appendLine("수정이 필요하지 않은 부분은 기존 내용을 그대로 유지해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
        }
    }

    private fun generateRegenerationPrompt(
        bmc: BusinessModelCanvas,
        additionalContext: String,
    ): String {
        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("기존 BMC를 참고하여 완전히 새로운 관점에서 BMC를 재생성해주세요.")
            appendLine()
            appendLine("기존 BMC (참고용):")
            appendLine("제목: ${bmc.title}")
            appendLine("1. 목표 고객: ${bmc.customerSegments ?: "없음"}")
            appendLine("2. 제공 가치: ${bmc.valueProposition ?: "없음"}")
            appendLine("3. 채널: ${bmc.channels ?: "없음"}")
            appendLine("4. 고객 관계: ${bmc.customerRelationships ?: "없음"}")
            appendLine("5. 수익 구조: ${bmc.revenueStreams ?: "없음"}")
            appendLine("6. 핵심 자원: ${bmc.keyResources ?: "없음"}")
            appendLine("7. 핵심 활동: ${bmc.keyActivities ?: "없음"}")
            appendLine("8. 핵심 파트너: ${bmc.keyPartners ?: "없음"}")
            appendLine("9. 비용 구조: ${bmc.costStructure ?: "없음"}")
            appendLine()
            appendLine("추가 고려사항:")
            appendLine(additionalContext)
            appendLine()
            appendLine("위 정보를 참고하여 더 구체적이고 실현 가능한 BMC를 새롭게 생성해주세요.")
            appendLine("기존 아이디어는 유지하되, 더 나은 접근 방식과 전략을 제시해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
        }
    }

    // ==================== Response Parsing ====================

    private fun parseBmcResponse(response: String): Map<String, String> {
        val bmcElements = mutableMapOf<String, String>()
        val lines = response.lines()

        val keys =
            listOf(
                "CUSTOMER_SEGMENTS",
                "VALUE_PROPOSITION",
                "CHANNELS",
                "CUSTOMER_RELATIONSHIPS",
                "REVENUE_STREAMS",
                "KEY_RESOURCES",
                "KEY_ACTIVITIES",
                "KEY_PARTNERS",
                "COST_STRUCTURE",
            )

        keys.forEach { key ->
            val content = extractContent(lines, key)
            if (content.isNotBlank()) {
                bmcElements[key] = content
            }
        }

        return bmcElements
    }

    private fun extractContent(
        lines: List<String>,
        key: String,
    ): String {
        val startIndex = lines.indexOfFirst { it.startsWith("$key:") }
        if (startIndex == -1) return ""

        val content = StringBuilder()
        var currentIndex = startIndex

        val firstLine = lines[currentIndex].removePrefix("$key:").trim()
        if (firstLine.isNotEmpty()) content.append(firstLine)

        currentIndex++
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            if (line.isEmpty()) {
                currentIndex++
                continue
            }

            if (line.contains(":") && line.split(":")[0].trim() in
                listOf(
                    "CUSTOMER_SEGMENTS", "VALUE_PROPOSITION", "CHANNELS", "CUSTOMER_RELATIONSHIPS",
                    "REVENUE_STREAMS", "KEY_RESOURCES", "KEY_ACTIVITIES", "KEY_PARTNERS", "COST_STRUCTURE",
                )
            ) {
                break
            }

            if (content.isNotEmpty()) content.append("\n")
            content.append(line)
            currentIndex++
        }

        return content.toString().trim()
    }
}
