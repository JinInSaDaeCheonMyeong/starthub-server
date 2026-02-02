package com.jininsadaecheonmyeong.starthubserver.infrastructure.listener.bmc

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.UserContextService
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
    private val userContextService: UserContextService,
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

            userContextService.embedUserContextAsync(event.user)
            logger.info("BMC 생성으로 인한 사용자 컨텍스트 임베딩 트리거 - BMC ID: {}, User ID: {}", bmcId, event.user.id)
        } catch (e: Exception) {
            logger.error("BMC 생성 이벤트 처리 실패 - BMC ID: {}", event.businessModelCanvas.id, e)
        }
    }
}
