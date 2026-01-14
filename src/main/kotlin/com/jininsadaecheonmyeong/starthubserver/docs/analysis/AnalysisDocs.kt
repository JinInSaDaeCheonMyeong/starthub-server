package com.jininsadaecheonmyeong.starthubserver.docs.analysis

import com.jininsadaecheonmyeong.starthubserver.dto.request.analysis.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.analysis.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "경쟁사 분석", description = "BMC 기반 경쟁사 분석 API")
interface AnalysisDocs {
    @Operation(
        summary = "경쟁사 분석 수행",
        description = "BMC를 기반으로 Perplexity AI를 활용한 실시간 웹 검색을 통해 경쟁사를 분석합니다. 검색 키워드를 직접 입력하지 않으면 BMC 내용을 기반으로 자동 생성됩니다. 분석 결과는 자동으로 저장됩니다.",
    )
    suspend fun analyzeCompetitors(
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

    @Operation(
        summary = "BMC별 경쟁사 분석 조회",
        description = "특정 BMC에 대해 저장된 경쟁사 분석 결과를 조회합니다.",
    )
    fun getAnalysisByBmcId(
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>>

    @Operation(
        summary = "사용자의 모든 경쟁사 분석 조회",
        description = "현재 사용자가 저장한 모든 경쟁사 분석 결과를 조회합니다.",
    )
    fun getAllAnalyses(): ResponseEntity<BaseResponse<List<CompetitorAnalysisResponse>>>

    @Operation(
        summary = "경쟁사 분석 재생성",
        description = "특정 BMC에 대한 경쟁사 분석을 다시 수행합니다. 기존 분석 결과가 있다면 덮어씁니다.",
    )
    suspend fun regenerateAnalysis(
        @Parameter(description = "BMC ID", required = true, example = "1")
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>>
}
