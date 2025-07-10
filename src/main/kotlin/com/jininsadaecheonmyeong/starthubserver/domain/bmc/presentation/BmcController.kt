package com.jininsadaecheonmyeong.starthubserver.domain.bmc.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.docs.BmcDocs
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcGenerationService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcModificationService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcQuestionService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BusinessModelCanvasService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/bmc")
class BmcController(
    private val bmcQuestionService: BmcQuestionService,
    private val bmcGenerationService: BmcGenerationService,
    private val businessModelCanvasService: BusinessModelCanvasService,
    private val bmcModificationService: BmcModificationService
) : BmcDocs {
    @GetMapping("/questions")
    override fun getBmcQuestions(): ResponseEntity<BaseResponse<List<String>>> {
        val questions = bmcQuestionService.getBmcQuestions()
        return BaseResponse.of(questions, "BMC 질문 목록 조회 성공")
    }

    @PostMapping("/sessions")
    override fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val response = bmcQuestionService.createBmcSession(request)
        return BaseResponse.of(response, "BMC 세션 생성 성공")
    }

    @GetMapping("/sessions")
    override fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>> {
        val sessions = bmcQuestionService.getAllBmcSessions()
        return BaseResponse.of(sessions, "BMC 세션 목록 조회 성공")
    }

    @GetMapping("/sessions/{sessionId}")
    override fun getBmcSession(
        @PathVariable sessionId: String
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val session = bmcQuestionService.getBmcSession(sessionId)
        return BaseResponse.of(session, "BMC 세션 조회 성공")
    }

    @PostMapping("/sessions/answer")
    override fun answerQuestion(
        @Valid @RequestBody request: AnswerQuestionRequest
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val response = bmcQuestionService.answerQuestion(request)
        return BaseResponse.of(response, "답변 저장 성공")
    }

    @PostMapping("/generate")
    override fun generateBmc(
        @Valid @RequestBody request: GenerateBmcRequest
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val response = bmcGenerationService.generateBusinessModelCanvas(request)
        return BaseResponse.of(response, "BMC 생성 성공")
    }

    @GetMapping("/canvases")
    override fun getAllBusinessModelCanvases(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>> {
        val canvases = businessModelCanvasService.getAllBusinessModelCanvases()
        return BaseResponse.of(canvases, "BMC 목록 조회 성공")
    }

    @GetMapping("/canvases/{id}")
    override fun getBusinessModelCanvas(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val canvas = businessModelCanvasService.getBusinessModelCanvas(id)
        return BaseResponse.of(canvas, "BMC 조회 성공")
    }

    @DeleteMapping("/canvases/{id}")
    override fun deleteBusinessModelCanvas(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<Unit>> {
        businessModelCanvasService.deleteBusinessModelCanvas(id)
        return BaseResponse.of("BMC 삭제 성공")
    }

    @PostMapping("/modify")
    override fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest
    ): ResponseEntity<BaseResponse<BmcModificationResponse>> {
        val response = bmcModificationService.requestBmcModification(request)
        return BaseResponse.of(response, "BMC 수정 완료")
    }

    @GetMapping("/canvases/{id}/history")
    override fun getBmcModificationHistory(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>> {
        val history = bmcModificationService.getBmcModificationHistory(id)
        return BaseResponse.of(history, "BMC 수정 히스토리 조회 성공")
    }
}