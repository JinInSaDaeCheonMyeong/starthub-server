package com.jininsadaecheonmyeong.starthubserver.infrastructure.scheduler.announcement

import com.jininsadaecheonmyeong.starthubserver.application.usecase.announcement.AnnouncementUseCase
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
