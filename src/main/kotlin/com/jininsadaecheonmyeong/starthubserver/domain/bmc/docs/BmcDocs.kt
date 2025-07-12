package com.jininsadaecheonmyeong.starthubserver.domain.bmc.docs

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Tag(name = "BMC", description = "비즈니스 모델 캔버스 관련 API")
interface BmcDocs {
    @Operation(
        summary = "BMC 질문 목록 조회",
        description = "BMC 생성을 위한 10개의 질문 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 질문 목록 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                        {
                            "data": [
                                "당신의 사업 아이디어로 해결하고자 하는 핵심 문제는 무엇입니까?",
                                "이 문제를 겪는 주요 고객층은 누구입니까? 구체적으로 설명해주세요.",
                                "당신의 제품/서비스가 제공하는 핵심 가치는 무엇입니까?",
                                "고객에게 제품/서비스를 전달하기 위한 주요 채널은 무엇입니까?",
                                "고객과 어떤 방식으로 관계를 형성하고 유지할 계획입니까?",
                                "이 사업을 운영하기 위해 필요한 핵심 자원은 무엇입니까?",
                                "사업 성공을 위해 반드시 수행해야 하는 핵심 활동은 무엇입니까?",
                                "사업 성공을 위해 협력해야 할 핵심 파트너는 누구입니까?",
                                "주요 비용 구조는 어떻게 구성될 예정입니까?",
                                "어떤 방식으로 수익을 창출할 계획입니까?"
                            ],
                            "status": "OK",
                            "message": "BMC 질문 목록 조회 성공"
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getBmcQuestions(): ResponseEntity<BaseResponse<List<String>>>

    @Operation(
        summary = "BMC 세션 생성",
        description = "새로운 BMC 대화 세션을 생성합니다. 사업 아이디어를 입력받아 세션을 시작합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 세션 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (사업 아이디어 필수)",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
        ],
    )
    fun createBmcSession(
        @Valid @RequestBody request: CreateBmcSessionRequest,
    ): ResponseEntity<BaseResponse<BmcSessionResponse>>

    @Operation(summary = "BMC 세션 목록 조회", description = "사용자의 모든 BMC 세션을 조회합니다.")
    fun getAllBmcSessions(): ResponseEntity<BaseResponse<List<BmcSessionResponse>>>

    @Operation(summary = "BMC 세션 상세 조회", description = "특정 BMC 세션의 상세 정보를 조회합니다.")
    fun getBmcSession(
        @PathVariable sessionId: String,
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

    @Operation(
        summary = "BMC 목록 조회",
        description = "사용자가 생성한 모든 BMC 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 목록 조회 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
        ],
    )
    fun getAllBusinessModelCanvases(): ResponseEntity<BaseResponse<List<BusinessModelCanvasResponse>>>

    @Operation(
        summary = "BMC 상세 조회",
        description = "특정 BMC의 상세 정보를 조회합니다. 9개 요소의 상세 내용을 확인할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음",
            ),
        ],
    )
    fun getBusinessModelCanvas(
        @PathVariable id: UUID,
    ): ResponseEntity<BaseResponse<BusinessModelCanvasResponse>>

    @Operation(
        summary = "BMC 삭제",
        description = "특정 BMC를 삭제합니다. Soft Delete로 처리됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 삭제 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음",
            ),
        ],
    )
    fun deleteBusinessModelCanvas(
        @PathVariable id: UUID,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "BMC 수정 요청", description = "생성된 BMC의 특정 부분을 수정하거나 전체를 재생성합니다.")
    fun modifyBmc(
        @Valid @RequestBody request: ModifyBmcRequest,
    ): ResponseEntity<BaseResponse<BmcModificationResponse>>

    @Operation(summary = "BMC 수정 히스토리 조회", description = "특정 BMC의 수정 요청 히스토리를 조회합니다.")
    fun getBmcModificationHistory(
        @PathVariable id: UUID,
    ): ResponseEntity<BaseResponse<List<BmcModificationResponse>>>
}
