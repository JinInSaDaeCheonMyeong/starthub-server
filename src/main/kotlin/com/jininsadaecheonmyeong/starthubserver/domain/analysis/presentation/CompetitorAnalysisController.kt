package com.jininsadaecheonmyeong.starthubserver.domain.analysis.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.docs.CompetitorAnalysisDocs
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.service.CompetitorAnalysisService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/analysis")
class CompetitorAnalysisController(
    private val competitorAnalysisService: CompetitorAnalysisService,
) : CompetitorAnalysisDocs {
    @PostMapping("/competitors")
    override fun analyzeCompetitors(
        @Valid @RequestBody request: CompetitorAnalysisRequest,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = competitorAnalysisService.analyzeCompetitors(request)
        return BaseResponse.of(result, "경쟁사 분석 완료")
    }

    @GetMapping("/competitors/bmc/{bmcId}")
    override fun getAnalysisByBmcId(
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = competitorAnalysisService.getAnalysisByBmcId(bmcId)
        return BaseResponse.of(result, "경쟁사 분석 조회 성공")
    }

    @GetMapping("/competitors")
    override fun getAllAnalyses(): ResponseEntity<BaseResponse<List<CompetitorAnalysisResponse>>> {
        val result = competitorAnalysisService.getAllAnalysesByUser()
        return BaseResponse.of(result, "경쟁사 분석 목록 조회 성공")
    }

    @PostMapping("/competitors/bmc/{bmcId}/regenerate")
    override fun regenerateAnalysis(
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = competitorAnalysisService.regenerateAnalysis(bmcId)
        return BaseResponse.of(result, "경쟁사 분석 재생성 완료")
    }
}
