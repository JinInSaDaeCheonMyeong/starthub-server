package com.jininsadaecheonmyeong.starthubserver.domain.bmc.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "BMC API", description = "비즈니스 모델 캔버스 관리 API")
interface BmcDocs {

    @Operation(
        summary = "BMC 질문 목록 조회",
        description = "BMC 생성을 위한 10개의 질문 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 질문 목록 조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
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
                        """
                    )]
                )]
            )
        ]
    )
    fun getBmcQuestions()

    @Operation(
        summary = "BMC 세션 생성",
        description = "새로운 BMC 질문 세션을 생성합니다. 사업 아이디어를 입력받아 세션을 시작합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 세션 생성 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (사업 아이디어 필수)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요"
            )
        ]
    )
    fun createBmcSession()

    @Operation(
        summary = "질문 답변",
        description = "BMC 세션의 특정 질문에 답변합니다. 질문 번호(1-10)와 답변을 입력받습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "답변 저장 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (질문 번호 또는 답변 오류)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC 세션을 찾을 수 없음"
            )
        ]
    )
    fun answerQuestion()

    @Operation(
        summary = "BMC 생성",
        description = "완료된 질문 세션을 바탕으로 AI가 BMC를 생성합니다. 모든 질문에 답변이 완료되어야 합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 생성 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "질문 세션이 완료되지 않음"
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC 세션을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "AI 서비스 오류"
            )
        ]
    )
    fun generateBmc()

    @Operation(
        summary = "BMC 목록 조회",
        description = "사용자가 생성한 모든 BMC 목록을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 목록 조회 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요"
            )
        ]
    )
    fun getAllBusinessModelCanvases()

    @Operation(
        summary = "BMC 상세 조회",
        description = "특정 BMC의 상세 정보를 조회합니다. 9개 요소의 상세 내용을 확인할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 조회 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음"
            )
        ]
    )
    fun getBusinessModelCanvas()

    @Operation(
        summary = "BMC 삭제",
        description = "특정 BMC를 삭제합니다. 소프트 삭제로 처리됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "BMC 삭제 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음"
            )
        ]
    )
    fun deleteBusinessModelCanvas()
}