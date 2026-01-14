package com.jininsadaecheonmyeong.starthubserver.controller.bmc

import com.jininsadaecheonmyeong.starthubserver.docs.bmc.BmcDocs
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.UpdateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.UpdateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcFormResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.bmc.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.usecase.bmc.BmcUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/bmc")
class BmcController(
    private val bmcUseCase: BmcUseCase,
) : BmcDocs {
    @PostMapping("/sessions")
    override fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val (result, message) = bmcUseCase.createBmcSession(request)
        return BaseResponse.of(result, message)
    }

    @PostMapping("/sessions/answer")
    override fun answerQuestion(
        @Valid @RequestBody request: AnswerQuestionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val result = bmcUseCase.answerQuestion(request)
        return BaseResponse.of(result, "답변 저장 성공")
    }

    @PostMapping("/generate")
    override fun generateBmc(
        @Valid @RequestBody request: GenerateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val (result, message) = bmcUseCase.generateBusinessModelCanvas(request)
        return BaseResponse.of(result, message)
    }

    @PostMapping("/modify")
    override fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest,
    ): ResponseEntity<BaseResponse<BmcModificationResponse>> {
        val result = bmcUseCase.requestBmcModification(request)
        return BaseResponse.of(result, "BMC 수정 완료")
    }

    @DeleteMapping("/canvases/{id}")
    override fun deleteBmc(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        bmcUseCase.deleteBusinessModelCanvas(id)
        return BaseResponse.of("BMC 삭제 성공")
    }

    @GetMapping("/questions")
    override fun getBmcQuestions(): ResponseEntity<BaseResponse<List<BmcFormResponse>>> {
        val result = bmcUseCase.getBmcQuestions()
        return BaseResponse.of(result, "BMC 질문 목록 조회 성공")
    }

    @GetMapping("/sessions")
    override fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>> {
        val result = bmcUseCase.getAllBmcSessions()
        return BaseResponse.of(result, "BMC 세션 목록 조회 성공")
    }

    @GetMapping("/sessions/{sessionId}")
    override fun getBmcSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>> {
        val result = bmcUseCase.getBmcSession(sessionId)
        return BaseResponse.of(result, "BMC 세션 조회 성공")
    }

    @GetMapping("/canvases")
    override fun getAllBmcs(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>> {
        val result = bmcUseCase.getAllBusinessModelCanvases()
        return BaseResponse.of(result, "BMC 목록 조회 성공")
    }

    @GetMapping("/canvases/{id}")
    override fun getBmcs(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = bmcUseCase.getBusinessModelCanvas(id)
        return BaseResponse.of(result, "BMC 조회 성공")
    }

    @GetMapping("/canvases/{id}/history")
    override fun getBmcModificationHistory(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>> {
        val result = bmcUseCase.getBmcModificationHistory(id)
        return BaseResponse.of(result, "BMC 수정 히스토리 조회 성공")
    }

    @PutMapping("/canvases")
    override fun updateBmc(
        @Valid @RequestBody request: UpdateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = bmcUseCase.updateBusinessModelCanvas(request)
        return BaseResponse.of(result, "BMC 수정 성공")
    }

    @PostMapping("/canvases/{bmcId}/image")
    override fun uploadBmcImage(
        @PathVariable bmcId: Long,
        @RequestParam image: MultipartFile,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = bmcUseCase.uploadBmcImage(bmcId, image)
        return BaseResponse.of(result, "BMC 이미지 업로드 성공")
    }

    @PatchMapping("/sessions/{sessionId}")
    override fun updateSessionAnswersAndRegenerate(
        @PathVariable sessionId: Long,
        @Valid @RequestBody request: UpdateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>> {
        val result = bmcUseCase.updateSessionAnswersAndRegenerate(sessionId, request)
        return BaseResponse.of(result, "답변 수정 및 BMC 재생성 완료")
    }
}
