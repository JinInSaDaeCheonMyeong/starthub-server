package com.jininsadaecheonmyeong.starthubserver.controller.analysis

import com.jininsadaecheonmyeong.starthubserver.docs.analysis.AnalysisDocs
import com.jininsadaecheonmyeong.starthubserver.dto.request.analysis.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.analysis.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.usecase.analysis.AnalysisUseCase
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
class AnalysisController(
    private val analysisUseCase: AnalysisUseCase,
) : AnalysisDocs {
    @PostMapping("/competitors")
    override suspend fun analyzeCompetitors(
        @Valid @RequestBody request: CompetitorAnalysisRequest,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = analysisUseCase.analyzeCompetitors(request)
        return BaseResponse.of(result, "경쟁사 분석 완료")
    }

    @GetMapping("/competitors/bmc/{bmcId}")
    override fun getAnalysisByBmcId(
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = analysisUseCase.getAnalysisByBmcId(bmcId)
        return BaseResponse.of(result, "경쟁사 분석 조회 성공")
    }

    @GetMapping("/competitors")
    override fun getAllAnalyses(): ResponseEntity<BaseResponse<List<CompetitorAnalysisResponse>>> {
        val result = analysisUseCase.getAllAnalysesByUser()
        return BaseResponse.of(result, "경쟁사 분석 목록 조회 성공")
    }

    @PostMapping("/competitors/bmc/{bmcId}/regenerate")
    override suspend fun regenerateAnalysis(
        @PathVariable bmcId: Long,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        val result = analysisUseCase.regenerateAnalysis(bmcId)
        return BaseResponse.of(result, "경쟁사 분석 재생성 완료")
    }
}
