package com.jininsadaecheonmyeong.starthubserver.infrastructure.migration

import com.jininsadaecheonmyeong.starthubserver.application.usecase.announcement.AnnouncementUseCase
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AnnouncementScrapingRunner(
    private val announcementUseCase: AnnouncementUseCase,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        announcementUseCase.scrapeAndSaveAnnouncements()
    }
}