package com.jininsadaecheonmyeong.starthubserver.domain.bmc.listener

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.service.CompetitorAnalysisAsyncService
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.event.BmcCreatedEvent
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BmcCreatedEventListener(
    private val competitorAnalysisAsyncService: CompetitorAnalysisAsyncService,
) {
    private val logger = logger()

    @EventListener
    fun handleBmcCreatedEvent(event: BmcCreatedEvent) {
        try {
            competitorAnalysisAsyncService.performAnalysisAsync(
                user = event.user,
                businessModelCanvas = event.businessModelCanvas,
            )
        } catch (e: Exception) {
            logger.error("Failed to trigger async competitor analysis for BMC ID: {}", event.businessModelCanvas.id, e)
        }
    }
}
