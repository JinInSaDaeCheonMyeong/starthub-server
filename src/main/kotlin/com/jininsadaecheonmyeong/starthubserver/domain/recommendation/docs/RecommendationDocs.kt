package com.jininsadaecheonmyeong.starthubserver.domain.recommendation.docs

import com.jininsadaecheonmyeong.starthubserver.domain.recommendation.data.response.RecommendationResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Recommendation", description = "AI 기반 맞춤형 창업 지원 공고 추천 시스템")
interface RecommendationDocs {
    
    @Operation(
        summary = "사용자별 맞춤 공고 추천",
        description = """
            사용자의 관심사, 좋아요 이력, BMC 데이터를 분석하여 맞춤형 창업 지원 공고를 추천합니다.
            
            **추천 알고리즘:**
            - 사용자 자기소개 (10%)
            - 관심 분야 (10%) 
            - 좋아요한 공고들 (40%)
            - BMC 데이터 (40%)
            
            **특징:**
            - 페이지네이션 지원
            - 유사도 점수 포함 (0.000~1.000)
            - 접수 기간 만료 공고 자동 제외
            - 같은 기관 공고 중복 제한 (최대 3개)
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "추천 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BaseResponse::class),
                    examples = [ExampleObject(
                        name = "추천 성공 예시",
                        value = """
                        {
                          "success": true,
                          "data": {
                            "userId": 123,
                            "recommendations": [
                              {
                                "announcementId": 456,
                                "title": "2024년 창업도약패키지 사업 공고",
                                "organization": "중소벤처기업부",
                                "supportField": "IT/소프트웨어",
                                "region": "서울",
                                "similarityScore": 0.892
                              }
                            ],
                            "totalCount": 45,
                            "page": 1,
                            "pageSize": 10,
                            "totalPages": 5,
                            "hasNext": true,
                            "hasPrevious": false,
                            "generatedAt": "2024-03-15T10:30:00"
                          }
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (페이지 번호, 크기 등)",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                        {
                          "success": false,
                          "message": "페이지 크기는 1~50 사이여야 합니다."
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                        {
                          "success": false,
                          "message": "사용자를 찾을 수 없습니다: 123"
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "503",
                description = "AI 추천 서비스 일시 장애",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                        {
                          "success": false,
                          "message": "추천 서비스가 일시적으로 사용할 수 없습니다.",
                          "data": {
                            "userId": 123,
                            "recommendations": [],
                            "totalCount": 0
                          }
                        }
                        """
                    )]
                )]
            )
        ]
    )
    fun getRecommendations(
        @Parameter(description = "사용자 ID", example = "123") userId: Long,
        @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") page: Int,
        @Parameter(description = "페이지당 항목 수 (1-50)", example = "10") pageSize: Int,
        @Parameter(description = "활성 공고만 필터링", example = "true") filterActiveOnly: Boolean,
        @Parameter(description = "지역 필터 (선택사항)", example = "서울") filterRegion: String?,
        @Parameter(description = "지원 분야 필터 (선택사항)", example = "IT/소프트웨어") filterSupportField: String?
    ): ResponseEntity<BaseResponse<RecommendationResponse>>
    
    @Operation(
        summary = "내 맞춤 공고 추천",
        description = """
            현재 로그인한 사용자를 위한 맞춤형 추천입니다.
            JWT 토큰이 필요하며, 토큰에서 사용자 정보를 자동으로 추출합니다.
        """
    )
    fun getMyRecommendations(
        @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") page: Int,
        @Parameter(description = "페이지당 항목 수 (1-50)", example = "10") pageSize: Int,
        @Parameter(description = "활성 공고만 필터링", example = "true") filterActiveOnly: Boolean,
        @Parameter(description = "지역 필터 (선택사항)", example = "서울") filterRegion: String?,
        @Parameter(description = "지원 분야 필터 (선택사항)", example = "IT/소프트웨어") filterSupportField: String?
    ): ResponseEntity<BaseResponse<RecommendationResponse>>
    
    @Operation(
        summary = "추천 서비스 상태 확인",
        description = "AI 추천 시스템의 연결 상태와 서비스 가용성을 확인합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "서비스 정상",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                        {
                          "success": true,
                          "data": {
                            "status": "healthy",
                            "service": "AI Recommendation Service",
                            "timestamp": 1647334200000
                          }
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "503",
                description = "서비스 장애",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                        {
                          "success": false,
                          "message": "추천 서비스가 일시적으로 사용할 수 없습니다.",
                          "data": {
                            "status": "unhealthy",
                            "service": "AI Recommendation Service",
                            "timestamp": 1647334200000
                          }
                        }
                        """
                    )]
                )]
            )
        ]
    )
    fun checkRecommendationHealth(): ResponseEntity<BaseResponse<Map<String, Any>>>
}