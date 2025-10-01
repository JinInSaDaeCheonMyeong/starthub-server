package com.jininsadaecheonmyeong.starthubserver.domain.analysis.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.request.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.domain.analysis.service.CompetitorAnalysisService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/analysis")
class CompetitorAnalysisController(
    private val competitorAnalysisService: CompetitorAnalysisService,
) {
    private val logger = LoggerFactory.getLogger(CompetitorAnalysisController::class.java)

    @PostConstruct
    fun init() {
        logger.info("CompetitorAnalysisController initialized - endpoint: POST /analysis/competitors")
    }

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        logger.info("Test endpoint called")
        return ResponseEntity.ok("CompetitorAnalysisController is working!")
    }

    @PostMapping("/competitors")
    fun analyzeCompetitors(
        @Valid @RequestBody request: CompetitorAnalysisRequest,
    ): ResponseEntity<BaseResponse<CompetitorAnalysisResponse>> {
        logger.info("Received competitor analysis request for BMC ID: {}", request.bmcId)
        val result = competitorAnalysisService.analyzeCompetitors(request)
        return BaseResponse.of(result, "경쟁사 분석 완료")
    }
}
