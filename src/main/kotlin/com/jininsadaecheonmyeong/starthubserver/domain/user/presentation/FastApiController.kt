package com.jininsadaecheonmyeong.starthubserver.domain.user.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.FastApiIntegrationService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "FastAPI Integration", description = "FastAPI 서버와의 연동 API")
@RestController
@RequestMapping("/api/v1/fastapi")
class FastApiController(
    private val fastApiIntegrationService: FastApiIntegrationService,
    private val announcementRepository: AnnouncementRepository
) {
    
    @Operation(summary = "특정 사용자 관심사 데이터를 FastAPI로 전송")
    @PostMapping("/users/{userId}/interests")
    fun sendUserInterests(@PathVariable userId: Long): ResponseEntity<BaseResponse<Map<String, Any>>> {
        val success = fastApiIntegrationService.sendUserInterestsToFastApi(userId)
        
        return if (success) {
            ResponseEntity.ok(
                BaseResponse.success(
                    mapOf(
                        "userId" to userId,
                        "message" to "사용자 관심사 데이터가 FastAPI로 성공적으로 전송되었습니다."
                    )
                )
            )
        } else {
            ResponseEntity.internalServerError().body(
                BaseResponse.error("사용자 관심사 데이터 전송에 실패했습니다.")
            )
        }
    }
    
    @Operation(summary = "모든 사용자 관심사 데이터를 FastAPI로 전송")
    @PostMapping("/users/interests/batch")
    fun sendAllUsersInterests(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        val successCount = fastApiIntegrationService.sendAllUsersInterestsToFastApi()
        
        return ResponseEntity.ok(
            BaseResponse.success(
                mapOf(
                    "successCount" to successCount,
                    "message" to "총 ${successCount}명의 사용자 관심사 데이터가 FastAPI로 전송되었습니다."
                )
            )
        )
    }
    
    @Operation(summary = "공고 데이터를 배치로 FastAPI에 전송")
    @PostMapping("/announcements/batch")
    fun sendAnnouncementsBatch(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        val announcements = announcementRepository.findAll()
        val success = fastApiIntegrationService.sendAnnouncementsBatchToFastApi(announcements)
        
        return if (success) {
            ResponseEntity.ok(
                BaseResponse.success(
                    mapOf(
                        "count" to announcements.size,
                        "message" to "총 ${announcements.size}개의 공고가 FastAPI로 성공적으로 전송되었습니다."
                    )
                )
            )
        } else {
            ResponseEntity.internalServerError().body(
                BaseResponse.error("공고 배치 전송에 실패했습니다.")
            )
        }
    }
    
    @Operation(summary = "신규 공고들을 FastAPI에 전송")
    @PostMapping("/announcements/recent")
    fun sendRecentAnnouncements(
        @RequestParam(defaultValue = "24") hours: Long
    ): ResponseEntity<BaseResponse<Map<String, Any>>> {
        val recentAnnouncements = announcementRepository.findRecentAnnouncements(hours)
        val success = fastApiIntegrationService.sendAnnouncementsBatchToFastApi(recentAnnouncements)
        
        return if (success) {
            ResponseEntity.ok(
                BaseResponse.success(
                    mapOf(
                        "count" to recentAnnouncements.size,
                        "hours" to hours,
                        "message" to "최근 ${hours}시간 내 ${recentAnnouncements.size}개의 공고가 FastAPI로 전송되었습니다."
                    )
                )
            )
        } else {
            ResponseEntity.internalServerError().body(
                BaseResponse.error("신규 공고 전송에 실패했습니다.")
            )
        }
    }
}