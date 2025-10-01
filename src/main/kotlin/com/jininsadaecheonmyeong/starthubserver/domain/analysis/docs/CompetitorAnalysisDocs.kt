package com.jininsadaecheonmyeong.starthubserver.domain.analysis.docs

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "경쟁사 분석", description = "BMC 기반 경쟁사 분석 API")
interface CompetitorAnalysisDocs {
    @Operation(
        summary = "경쟁사 분석 수행",
        description = "BMC를 기반으로 Perplexity AI를 활용한 실시간 웹 검색을 통해 경쟁사를 분석합니다. 검색 키워드를 직접 입력하지 않으면 BMC 내용을 기반으로 자동 생성됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "경쟁사 분석 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CompetitorAnalysisResponse::class),
                        examples = [
                            ExampleObject(
                                name = "성공 응답 예시",
                                summary = "스타트업 BMC 경쟁사 분석 결과",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (BMC ID 누락, 유효하지 않은 파라미터 등)",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """{
  "statusCode": 400,
  "message": "BMC ID는 필수입니다.",
  "data": null
}""",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (다른 사용자의 BMC에 대한 접근)",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """{
  "statusCode": 403,
  "message": "접근 권한이 없습니다.",
  "data": null
}""",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "BMC를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """{
  "statusCode": 404,
  "message": "BMC를 찾을 수 없습니다.",
  "data": null
}""",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류 (Perplexity API 오류, 네트워크 오류 등)",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "일반 오류",
                                value = """{
  "statusCode": 500,
  "message": "경쟁사 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  "data": null
}""",
                            ),
                            ExampleObject(
                                name = "Perplexity API 오류",
                                value = """{
  "statusCode": 500,
  "message": "Perplexity API 토큰이 초과되었습니다. 잠시 후 다시 시도해주세요.",
  "data": null
}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/competitors")
    fun analyzeCompetitors(
        @Parameter(
            description = "경쟁사 분석 요청 정보",
            required = true,
            schema = Schema(implementation = CompetitorAnalysisRequest::class),
            example = """{
  "bmcId": 1,
  "searchKeywords": ["AI 학습", "온라인 교육", "에듀테크"]
}""",
        )
        @Valid
        @RequestBody request: CompetitorAnalysisRequest,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>>
}
