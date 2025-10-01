package com.jininsadaecheonmyeong.starthubserver.domain.analysis.docs

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
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
