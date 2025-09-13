package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserInterestsResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Service
class FastApiIntegrationService(
    private val userInterestsService: UserInterestsService,
    private val restTemplate: RestTemplate = RestTemplate()
) {
    
    @Value("\${fastapi.base-url:http://localhost:8001}")
    private lateinit var fastApiBaseUrl: String
    
    private val logger = LoggerFactory.getLogger(FastApiIntegrationService::class.java)
    
    fun sendUserInterestsToFastApi(userId: Long): Boolean {
        return try {
            val userInterestsData = userInterestsService.getUserInterestsData(userId)
            val url = "$fastApiBaseUrl/api/v1/users/interests"
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            
            val request = HttpEntity(userInterestsData, headers)
            val response = restTemplate.exchange(url, HttpMethod.POST, request, Map::class.java)
            
            logger.info("사용자 $userId 의 관심사 데이터를 FastAPI로 전송 완료: ${response.statusCode}")
            response.statusCode.is2xxSuccessful
            
        } catch (e: Exception) {
            logger.error("사용자 $userId 의 관심사 데이터 전송 실패", e)
            false
        }
    }
    
    fun sendAllUsersInterestsToFastApi(): Int {
        val allUsersData = userInterestsService.getAllUsersInterestsData()
        var successCount = 0
        
        allUsersData.forEach { userInterestsData ->
            try {
                val url = "$fastApiBaseUrl/api/v1/users/interests"
                
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                
                val request = HttpEntity(userInterestsData, headers)
                val response = restTemplate.exchange(url, HttpMethod.POST, request, Map::class.java)
                
                if (response.statusCode.is2xxSuccessful) {
                    successCount++
                    logger.info("사용자 ${userInterestsData.userProfile.userId} 관심사 데이터 전송 완료")
                }
                
            } catch (e: Exception) {
                logger.error("사용자 ${userInterestsData.userProfile.userId} 관심사 데이터 전송 실패", e)
            }
        }
        
        logger.info("전체 사용자 관심사 데이터 전송 완료: $successCount/${allUsersData.size}")
        return successCount
    }
    
    fun sendAnnouncementsBatchToFastApi(announcements: List<Announcement>): Boolean {
        return try {
            val announcementDtos = announcements.map { announcement ->
                mapOf(
                    "id" to announcement.id,
                    "title" to announcement.title,
                    "organization" to announcement.organization,
                    "support_field" to announcement.supportField,
                    "target_age" to announcement.targetAge,
                    "region" to announcement.region,
                    "content" to announcement.content
                )
            }
            
            val batchData = mapOf(
                "announcements" to announcementDtos,
                "batch_id" to "batch_${System.currentTimeMillis()}",
                "timestamp" to LocalDateTime.now().toString()
            )
            
            val url = "$fastApiBaseUrl/api/v1/announcements/batch"
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            
            val request = HttpEntity(batchData, headers)
            val response = restTemplate.exchange(url, HttpMethod.POST, request, Map::class.java)
            
            logger.info("공고 배치 데이터를 FastAPI로 전송 완료: ${announcements.size}개, 상태: ${response.statusCode}")
            response.statusCode.is2xxSuccessful
            
        } catch (e: Exception) {
            logger.error("공고 배치 데이터 전송 실패", e)
            false
        }
    }
}