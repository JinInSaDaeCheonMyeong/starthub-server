package com.jininsadaecheonmyeong.starthubserver.domain.announcement.scheduler

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.FastApiIntegrationService
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.UserChangeTrackingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScrapingScheduler(
    private val scrapingService: AnnouncementService,
    private val fastApiIntegrationService: FastApiIntegrationService,
    private val userChangeTrackingService: UserChangeTrackingService
) {
    
    private val logger = LoggerFactory.getLogger(ScrapingScheduler::class.java)
    
    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyScraping() {
        scrapingService.scrapeAndSaveAnnouncements()
        
        try {
            val announcements = scrapingService.getAnnouncementsFromLastHours(24)
            if (announcements.isNotEmpty()) {
                fastApiIntegrationService.sendAnnouncementsBatchToFastApi(announcements)
                logger.info("신규 스크래핑된 ${announcements.size}개 공고를 AI 서버로 전송 완료")
            }
        } catch (e: Exception) {
            logger.error("신규 공고 AI 서버로 전송 실패", e)
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailySoftDeletion() {
        scrapingService.softDeleteExpiredAnnouncements()
    }
    
    @Scheduled(cron = "0 0 6 * * *")
    fun syncUserInterestsDaily() {
        try {
            val successCount = fastApiIntegrationService.sendAllUsersInterestsToFastApi()
            logger.info("일일 사용자 관심사 동기화 완료: ${successCount}명")
        } catch (e: Exception) {
            logger.error("일일 사용자 관심사 동기화 실패", e)
        }
    }
    
    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    fun processUserChangesHourly() {
        try {
            val changedUsersCount = userChangeTrackingService.getChangedUsersCount()
            
            if (changedUsersCount > 0) {
                logger.info("변경된 사용자 ${changedUsersCount}명의 데이터를 AI 서버로 전송 시작")
                
                val successCount = userChangeTrackingService.processChangedUsers()
                logger.info("1시간 단위 사용자 변경사항 처리 완료: ${successCount}/${changedUsersCount}")
            } else {
                logger.debug("변경된 사용자가 없어 스킵합니다.")
            }
        } catch (e: Exception) {
            logger.error("1시간 단위 사용자 변경사항 처리 실패", e)
        }
    }
}
