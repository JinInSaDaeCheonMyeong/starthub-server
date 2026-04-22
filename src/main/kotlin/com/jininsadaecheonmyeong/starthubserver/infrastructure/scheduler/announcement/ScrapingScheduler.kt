package com.jininsadaecheonmyeong.starthubserver.infrastructure.scheduler.announcement

import com.jininsadaecheonmyeong.starthubserver.application.usecase.announcement.AnnouncementUseCase
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScrapingScheduler(
    private val announcementUseCase: AnnouncementUseCase,
) {
    @PostConstruct
    fun onStartup() {
        announcementUseCase.scrapeAndSaveAnnouncements()
        announcementUseCase.deactivateExpiredAnnouncements()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyScraping() {
        announcementUseCase.scrapeAndSaveAnnouncements()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun runDailyDeactivation() {
        announcementUseCase.deactivateExpiredAnnouncements()
    }
}
