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
            logger.error("경쟁사 분석 비동기 실행 실패 - BMC ID: {}", event.businessModelCanvas.id, e)
        }
    }
}
