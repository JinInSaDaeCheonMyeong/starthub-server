package com.jininsadaecheonmyeong.starthubserver.docs.bmc

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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Tag(name = "BMC", description = "비즈니스 모델 캔버스 관련 API")
interface BmcDocs {
    @Operation(
        summary = "BMC 세션 생성",
        description = "새로운 BMC 대화 세션을 생성합니다. 제목을 입력받아 세션을 시작합니다.",
    )
    fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>>

    @Operation(summary = "질문 답변", description = "BMC 세션의 특정 질문에 답변합니다.")
    fun answerQuestion(
        @Valid @RequestBody request: AnswerQuestionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>>

    @Operation(
        summary = "BMC 생성",
        description = "완료된 질문 세션을 바탕으로 AI가 BMC를 생성합니다. 모든 질문에 답변이 완료되어야 합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "질문 세션이 완료되지 않음",
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC 세션을 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "500",
                description = "AI 서비스 오류",
            ),
        ],
    )
    fun generateBmc(
        @Valid @RequestBody request: GenerateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>

    @Operation(summary = "BMC 수정 요청", description = "생성된 BMC의 특정 부분을 수정하거나 전체를 재생성합니다.")
    fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest,
    ): ResponseEntity<BaseResponse<BmcModificationResponse>>

    @Operation(
        summary = "BMC 삭제",
        description = "특정 BMC를 삭제합니다. Soft Delete로 처리됩니다.",
    )
    fun deleteBmc(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "BMC 질문 목록 조회",
        description = "BMC 생성을 위한 10개의 질문 목록을 조회합니다.",
    )
    fun getBmcQuestions(): ResponseEntity<BaseResponse<List<BmcFormResponse>>>

    @Operation(summary = "BMC 세션 목록 조회", description = "사용자의 모든 BMC 세션을 조회합니다.")
    fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>>

    @Operation(summary = "BMC 세션 상세 조회", description = "특정 BMC 세션의 상세 정보를 조회합니다.")
    fun getBmcSession(
        @PathVariable sessionId: Long,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>>

    @Operation(
        summary = "BMC 목록 조회",
        description = "사용자가 생성한 모든 BMC 목록을 조회합니다.",
    )
    fun getAllBmcs(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>>

    @Operation(
        summary = "BMC 상세 조회",
        description = "특정 BMC의 상세 정보를 조회합니다. 9개 요소의 상세 내용을 확인할 수 있습니다.",
    )
    fun getBmcs(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>

    @Operation(summary = "BMC 수정 히스토리 조회", description = "특정 BMC의 수정 요청 히스토리를 조회합니다.")
    fun getBmcModificationHistory(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>>

    @Operation(
        summary = "BMC 직접 수정",
        description = "사용자가 직접 BMC 내용을 수정합니다. 수정하고 싶은 필드만 입력합니다.",
    )
    fun updateBmc(
        @Valid @RequestBody request: UpdateBmcRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>

    @Operation(
        summary = "BMC 이미지 업로드",
        description = "완성된 BMC를 이미지로 업로드하여 GCS에 저장합니다. BMC ID와 이미지 파일을 전송합니다.",
    )
    fun uploadBmcImage(
        @PathVariable bmcId: Long,
        @RequestParam image: MultipartFile,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>

    @Operation(
        summary = "세션 답변 수정 후 BMC 재생성",
        description = "세션의 답변을 수정하고 수정된 답변으로 새로운 BMC를 생성합니다. 수정할 질문들의 번호와 답변을 리스트로 입력합니다.",
    )
    fun updateSessionAnswersAndRegenerate(
        @PathVariable sessionId: Long,
        @Valid @RequestBody request: UpdateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>
}
