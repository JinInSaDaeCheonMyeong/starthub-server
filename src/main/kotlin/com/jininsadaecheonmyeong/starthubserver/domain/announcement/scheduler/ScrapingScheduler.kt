package com.jininsadaecheonmyeong.starthubserver.domain.announcement.scheduler

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScrapingScheduler(
    private val scrapingService: AnnouncementService,
) {
    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyScraping() {
        scrapingService.scrapeAndSaveAnnouncements()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailySoftDeletion() {
        scrapingService.softDeleteExpiredAnnouncements()
    }
}
