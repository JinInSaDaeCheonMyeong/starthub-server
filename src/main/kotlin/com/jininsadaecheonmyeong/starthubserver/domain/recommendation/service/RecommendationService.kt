package com.jininsadaecheonmyeong.starthubserver.domain.recommendation.service

import com.jininsadaecheonmyeong.starthubserver.domain.recommendation.data.response.*
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class RecommendationService(
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate = RestTemplate()
) {
    
    @Value("\${fastapi.base-url:http://localhost:8001}")
    private lateinit var fastApiBaseUrl: String
    
    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)
    
    fun getRecommendationsForUser(
        userId: Long,
        page: Int = 1,
        pageSize: Int = 10,
        filterActiveOnly: Boolean = true,
        filterRegion: String? = null,
        filterSupportField: String? = null
    ): RecommendationResponse {
        
        // 사용자 존재 확인
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("사용자를 찾을 수 없습니다: $userId") }
        
        return try {
            logger.info("사용자 추천 요청: userId=$userId, page=$page, pageSize=$pageSize")
            
            // FastAPI 호출 URL 생성
            val url = UriComponentsBuilder.fromHttpUrl("$fastApiBaseUrl/api/v1/users/$userId/recommendations")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .queryParam("filter_active_only", filterActiveOnly)
                .apply {
                    if (!filterRegion.isNullOrBlank()) {
                        queryParam("filter_region", filterRegion)
                    }
                    if (!filterSupportField.isNullOrBlank()) {
                        queryParam("filter_support_field", filterSupportField)
                    }
                }
                .build()
                .toUriString()
            
            val headers = HttpHeaders()
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            
            val request = HttpEntity<Void>(headers)
            
            // FastAPI 호출
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                FastApiRecommendationResponse::class.java
            )
            
            // 응답 매핑
            val fastApiResponse = response.body 
                ?: throw RuntimeException("FastAPI에서 빈 응답을 받았습니다")
            
            val mappedResponse = mapFastApiResponseToRecommendationResponse(fastApiResponse)
            
            logger.info("사용자 추천 완료: userId=$userId, 추천수=${mappedResponse.recommendations.size}")
            
            mappedResponse
            
        } catch (e: Exception) {
            logger.error("사용자 추천 조회 실패: userId=$userId", e)
            
            // FastAPI 오류 시 빈 추천 응답 반환
            createEmptyRecommendationResponse(
                userId = userId,
                page = page,
                pageSize = pageSize,
                filterActiveOnly = filterActiveOnly,
                filterRegion = filterRegion,
                filterSupportField = filterSupportField
            )
        }
    }
    
    private fun mapFastApiResponseToRecommendationResponse(
        fastApiResponse: FastApiRecommendationResponse
    ): RecommendationResponse {
        
        val mappedRecommendations = fastApiResponse.recommendations.map { announcement ->
            RecommendedAnnouncementResponse(
                announcementId = announcement.announcementId,
                title = announcement.title,
                organization = announcement.organization,
                supportField = announcement.supportField,
                targetAge = announcement.targetAge,
                region = announcement.region,
                similarityScore = announcement.similarityScore,
                metadata = announcement.metadata
            )
        }
        
        val mappedFilters = fastApiResponse.filtersApplied?.let { filters ->
            RecommendationFilters(
                activeOnly = filters.activeOnly,
                region = filters.region,
                supportField = filters.supportField
            )
        }
        
        return RecommendationResponse(
            userId = fastApiResponse.userId,
            recommendations = mappedRecommendations,
            totalCount = fastApiResponse.totalCount,
            page = fastApiResponse.page,
            pageSize = fastApiResponse.pageSize,
            totalPages = fastApiResponse.totalPages,
            hasNext = fastApiResponse.hasNext,
            hasPrevious = fastApiResponse.hasPrevious,
            generatedAt = parseGeneratedAt(fastApiResponse.generatedAt),
            filtersApplied = mappedFilters
        )
    }
    
    private fun parseGeneratedAt(generatedAt: String): LocalDateTime {
        return try {
            // ISO 형식 파싱 시도
            LocalDateTime.parse(generatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            // 파싱 실패 시 현재 시간 반환
            logger.warn("생성 시간 파싱 실패: $generatedAt, 현재 시간으로 대체")
            LocalDateTime.now()
        }
    }
    
    private fun createEmptyRecommendationResponse(
        userId: Long,
        page: Int,
        pageSize: Int,
        filterActiveOnly: Boolean,
        filterRegion: String?,
        filterSupportField: String?
    ): RecommendationResponse {
        
        return RecommendationResponse(
            userId = userId,
            recommendations = emptyList(),
            totalCount = 0,
            page = page,
            pageSize = pageSize,
            totalPages = 0,
            hasNext = false,
            hasPrevious = false,
            generatedAt = LocalDateTime.now(),
            filtersApplied = RecommendationFilters(
                activeOnly = filterActiveOnly,
                region = filterRegion,
                supportField = filterSupportField
            )
        )
    }
    
    fun checkRecommendationServiceHealth(): Boolean {
        return try {
            val url = "$fastApiBaseUrl/health"
            val response = restTemplate.getForEntity(url, Map::class.java)
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error("FastAPI 헬스체크 실패", e)
            false
        }
    }
}