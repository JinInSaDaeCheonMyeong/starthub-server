package com.jininsadaecheonmyeong.starthubserver.domain.analysis.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.docs.CompetitorAnalysisDocs
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.service.CompetitorAnalysisService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/analysis")
class CompetitorAnalysisController(
    private val competitorAnalysisService: CompetitorAnalysisService,
) : CompetitorAnalysisDocs {
    override fun analyzeCompetitors(
        @Valid @RequestBody request: CompetitorAnalysisRequest,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = competitorAnalysisService.analyzeCompetitors(request)
        return BaseResponse.of(result, "경쟁사 분석 완료")
    }
}
