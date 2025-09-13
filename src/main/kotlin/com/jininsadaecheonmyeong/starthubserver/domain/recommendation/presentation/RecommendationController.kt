package com.jininsadaecheonmyeong.starthubserver.domain.recommendation.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.recommendation.data.response.RecommendationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recommendation.docs.RecommendationDocs
import com.jininsadaecheonmyeong.starthubserver.domain.recommendation.service.RecommendationService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Recommendation", description = "AI 추천 시스템 API")
@RestController
@RequestMapping("/api/v1/recommendations")
class RecommendationController(
    private val recommendationService: RecommendationService
) : RecommendationDocs {
    
    @GetMapping("/{userId}")
    override fun getRecommendations(
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") 
        @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "페이지당 항목 수 (1-50)", example = "10") 
        @RequestParam(defaultValue = "10") pageSize: Int,
        @Parameter(description = "활성 공고만 필터링", example = "true") 
        @RequestParam(defaultValue = "true") filterActiveOnly: Boolean,
        @Parameter(description = "지역 필터", example = "서울") 
        @RequestParam(required = false) filterRegion: String?,
        @Parameter(description = "지원 분야 필터", example = "IT/소프트웨어") 
        @RequestParam(required = false) filterSupportField: String?
    ): ResponseEntity<BaseResponse<RecommendationResponse>> {
        
        // 입력값 검증
        if (page < 1) {
            return ResponseEntity.badRequest().body(
                BaseResponse.error("페이지 번호는 1 이상이어야 합니다.")
            )
        }
        
        if (pageSize < 1 || pageSize > 50) {
            return ResponseEntity.badRequest().body(
                BaseResponse.error("페이지 크기는 1~50 사이여야 합니다.")
            )
        }
        
        val recommendations = recommendationService.getRecommendationsForUser(
            userId = userId,
            page = page,
            pageSize = pageSize,
            filterActiveOnly = filterActiveOnly,
            filterRegion = filterRegion,
            filterSupportField = filterSupportField
        )
        
        return ResponseEntity.ok(BaseResponse.success(recommendations))
    }
    
    @GetMapping("/my")
    override fun getMyRecommendations(
        @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") 
        @RequestParam(defaultValue = "1") page: Int,
        @Parameter(description = "페이지당 항목 수 (1-50)", example = "10") 
        @RequestParam(defaultValue = "10") pageSize: Int,
        @Parameter(description = "활성 공고만 필터링", example = "true") 
        @RequestParam(defaultValue = "true") filterActiveOnly: Boolean,
        @Parameter(description = "지역 필터", example = "서울") 
        @RequestParam(required = false) filterRegion: String?,
        @Parameter(description = "지원 분야 필터", example = "IT/소프트웨어") 
        @RequestParam(required = false) filterSupportField: String?
    ): ResponseEntity<BaseResponse<RecommendationResponse>> {
        
        val currentUser = UserAuthenticationHolder.current()
        
        return getRecommendations(
            userId = currentUser.id!!,
            page = page,
            pageSize = pageSize,
            filterActiveOnly = filterActiveOnly,
            filterRegion = filterRegion,
            filterSupportField = filterSupportField
        )
    }
    
    @GetMapping("/health")
    override fun checkRecommendationHealth(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        
        val isHealthy = recommendationService.checkRecommendationServiceHealth()
        
        val healthStatus = mapOf(
            "status" to if (isHealthy) "healthy" else "unhealthy",
            "service" to "AI Recommendation Service",
            "timestamp" to System.currentTimeMillis()
        )
        
        return if (isHealthy) {
            ResponseEntity.ok(BaseResponse.success(healthStatus))
        } else {
            ResponseEntity.status(503).body(
                BaseResponse.error("추천 서비스가 일시적으로 사용할 수 없습니다.", healthStatus)
            )
        }
    }
}