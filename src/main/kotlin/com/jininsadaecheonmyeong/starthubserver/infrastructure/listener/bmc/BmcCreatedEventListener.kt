package com.jininsadaecheonmyeong.starthubserver.infrastructure.listener.bmc

import com.jininsadaecheonmyeong.starthubserver.application.usecase.analysis.AnalysisUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.event.bmc.BmcCreatedEvent
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BmcCreatedEventListener(
    private val analysisUseCase: AnalysisUseCase,
    private val domainBmcRepository: BusinessModelCanvasRepository,
) {
    private val logger = logger()

    @EventListener
    fun handleBmcCreatedEvent(event: BmcCreatedEvent) {
        try {
            val bmcId = event.businessModelCanvas.id ?: return
            val domainBmc =
                domainBmcRepository.findByIdAndDeletedFalse(bmcId)
                    .orElse(null) ?: return

            analysisUseCase.performAnalysisAsync(
                user = event.user,
                businessModelCanvas = domainBmc,
            )
        } catch (e: Exception) {
            logger.error("경쟁사 분석 비동기 실행 실패 - BMC ID: {}", event.businessModelCanvas.id, e)
        }
    }
}
