package com.jininsadaecheonmyeong.starthubserver.domain.bmc.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcGenerationService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcModificationService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BmcQuestionService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.service.BusinessModelCanvasService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "BMC API", description = "비즈니스 모델 캔버스 관리 API")
@RestController
@RequestMapping("/bmc")
class BmcController(
    private val bmcQuestionService: BmcQuestionService,
    private val bmcGenerationService: BmcGenerationService,
    private val businessModelCanvasService: BusinessModelCanvasService,
    private val bmcModificationService: BmcModificationService
) {

    @Operation(summary = "BMC 질문 목록 조회", description = "BMC 생성을 위한 질문 목록을 조회합니다.")
    @GetMapping("/questions")
    fun getBmcQuestions(): ResponseEntity<BaseResponse<List<String>>> {
        val questions = bmcQuestionService.getBmcQuestions()
        return BaseResponse.of(questions, "BMC 질문 목록 조회 성공")
    }

    @Operation(summary = "BMC 세션 생성", description = "새로운 BMC 질문 세션을 생성합니다.")
    @PostMapping("/sessions")
    fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val response = bmcQuestionService.createBmcSession(request)
        return BaseResponse.of(response, "BMC 세션 생성 성공")
    }

    @Operation(summary = "BMC 세션 목록 조회", description = "사용자의 모든 BMC 세션을 조회합니다.")
    @GetMapping("/sessions")
    fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>> {
        val sessions = bmcQuestionService.getAllBmcSessions()
        return BaseResponse.of(sessions, "BMC 세션 목록 조회 성공")
    }

    @Operation(summary = "BMC 세션 상세 조회", description = "특정 BMC 세션의 상세 정보를 조회합니다.")
    @GetMapping("/sessions/{sessionId}")
    fun getBmcSession(
        @PathVariable sessionId: String
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val session = bmcQuestionService.getBmcSession(sessionId)
        return BaseResponse.of(session, "BMC 세션 조회 성공")
    }

    @Operation(summary = "질문 답변", description = "BMC 세션의 특정 질문에 답변합니다.")
    @PostMapping("/sessions/answer")
    fun answerQuestion(
        @Valid @RequestBody request: AnswerQuestionRequest
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val response = bmcQuestionService.answerQuestion(request)
        return BaseResponse.of(response, "답변 저장 성공")
    }

    @Operation(summary = "BMC 생성", description = "완료된 질문 세션을 바탕으로 BMC를 생성합니다.")
    @PostMapping("/generate")
    fun generateBmc(
        @Valid @RequestBody request: GenerateBmcRequest
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val response = bmcGenerationService.generateBusinessModelCanvas(request)
        return BaseResponse.of(response, "BMC 생성 성공")
    }

    @Operation(summary = "BMC 목록 조회", description = "사용자의 모든 BMC를 조회합니다.")
    @GetMapping("/canvases")
    fun getAllBusinessModelCanvases(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>> {
        val canvases = businessModelCanvasService.getAllBusinessModelCanvases()
        return BaseResponse.of(canvases, "BMC 목록 조회 성공")
    }

    @Operation(summary = "BMC 상세 조회", description = "특정 BMC의 상세 정보를 조회합니다.")
    @GetMapping("/canvases/{id}")
    fun getBusinessModelCanvas(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val canvas = businessModelCanvasService.getBusinessModelCanvas(id)
        return BaseResponse.of(canvas, "BMC 조회 성공")
    }

    @Operation(summary = "BMC 삭제", description = "특정 BMC를 삭제합니다.")
    @DeleteMapping("/canvases/{id}")
    fun deleteBusinessModelCanvas(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<Unit>> {
        businessModelCanvasService.deleteBusinessModelCanvas(id)
        return BaseResponse.of("BMC 삭제 성공")
    }

    @Operation(summary = "BMC 수정 요청", description = "생성된 BMC의 특정 부분을 수정하거나 전체를 재생성합니다.")
    @PostMapping("/modify")
    fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest
    ): ResponseEntity<BaseResponse<BmcModificationResponse>> {
        val response = bmcModificationService.requestBmcModification(request)
        return BaseResponse.of(response, "BMC 수정 완료")
    }

    @Operation(summary = "BMC 수정 히스토리 조회", description = "특정 BMC의 수정 요청 히스토리를 조회합니다.")
    @GetMapping("/canvases/{id}/modifications")
    fun getBmcModificationHistory(
        @PathVariable id: UUID
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>> {
        val history = bmcModificationService.getBmcModificationHistory(id)
        return BaseResponse.of(history, "BMC 수정 히스토리 조회 성공")
    }
}