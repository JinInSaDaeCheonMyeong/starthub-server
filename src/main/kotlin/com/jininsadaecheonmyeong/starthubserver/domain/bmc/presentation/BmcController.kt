package com.jininsadaecheonmyeong.starthubserver.domain.bmc.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.UpdateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcFormResponse
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bmc")
class BmcController(
    private val bmcQuestionService: BmcQuestionService,
    private val bmcGenerationService: BmcGenerationService,
    private val businessModelCanvasService: BusinessModelCanvasService,
    private val bmcModificationService: BmcModificationService,
) : BmcDocs {
    @PostMapping("/sessions")
    override fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val (result, message) = bmcQuestionService.createBmcSession(request)
        return BaseResponse.of(result, message)
    }

    @PostMapping("/sessions/answer")
    override fun answerQuestion(
        @Valid @RequestBody request: AnswerQuestionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val result = bmcQuestionService.answerQuestion(request)
        return BaseResponse.of(result, "답변 저장 성공")
    }

    @PostMapping("/generate")
    override fun generateBmc(
        @Valid @RequestBody request: GenerateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val (result, message) = bmcGenerationService.generateBusinessModelCanvas(request)
        return BaseResponse.of(result, message)
    }

    @PostMapping("/modify")
    override fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest,
    ): ResponseEntity<BaseResponse<BmcModificationResponse>> {
        val result = bmcModificationService.requestBmcModification(request)
        return BaseResponse.of(result, "BMC 수정 완료")
    }

    @DeleteMapping("/canvases/{id}")
    override fun deleteBmc(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        businessModelCanvasService.deleteBusinessModelCanvas(id)
        return BaseResponse.of("BMC 삭제 성공")
    }

    @GetMapping("/questions")
    override fun getBmcQuestions(): ResponseEntity<BaseResponse<List<BmcFormResponse>>> {
        val result = bmcQuestionService.getBmcQuestions()
        return BaseResponse.of(result, "BMC 질문 목록 조회 성공")
    }

    @GetMapping("/sessions")
    override fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>> {
        val result = bmcQuestionService.getAllBmcSessions()
        return BaseResponse.of(result, "BMC 세션 목록 조회 성공")
    }

    @GetMapping("/sessions/{sessionId}")
    override fun getBmcSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val result = bmcQuestionService.getBmcSession(sessionId)
        return BaseResponse.of(result, "BMC 세션 조회 성공")
    }

    @GetMapping("/canvases")
    override fun getAllBmcs(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>> {
        val result = businessModelCanvasService.getAllBusinessModelCanvases()
        return BaseResponse.of(result, "BMC 목록 조회 성공")
    }

    @GetMapping("/canvases/{id}")
    override fun getBmcs(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = businessModelCanvasService.getBusinessModelCanvas(id)
        return BaseResponse.of(result, "BMC 조회 성공")
    }

    @GetMapping("/canvases/{id}/history")
    override fun getBmcModificationHistory(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>> {
        val result = bmcModificationService.getBmcModificationHistory(id)
        return BaseResponse.of(result, "BMC 수정 히스토리 조회 성공")
    }

    @PutMapping("/canvases")
    override fun updateBmc(
        @Valid @RequestBody request: UpdateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = businessModelCanvasService.updateBusinessModelCanvas(request)
        return BaseResponse.of(result, "BMC 수정 성공")
    }
}
