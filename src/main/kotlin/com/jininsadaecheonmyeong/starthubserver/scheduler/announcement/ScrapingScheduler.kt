package com.jininsadaecheonmyeong.starthubserver.scheduler.announcement

import com.jininsadaecheonmyeong.starthubserver.usecase.announcement.AnnouncementUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScrapingScheduler(
    private val announcementUseCase: AnnouncementUseCase,
) {
    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyScraping() {
        announcementUseCase.scrapeAndSaveAnnouncements()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyDeactivation() {
        announcementUseCase.deactivateExpiredAnnouncements()
    }
}
