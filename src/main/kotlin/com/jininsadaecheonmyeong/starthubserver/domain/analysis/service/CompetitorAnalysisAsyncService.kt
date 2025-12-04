package com.jininsadaecheonmyeong.starthubserver.domain.analysis.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class CompetitorAnalysisAsyncService(
    private val competitorAnalysisService: CompetitorAnalysisService,
    private val taskManager: CompetitorAnalysisTaskManager,
) {
    private val logger = logger()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun performAnalysisAsync(
        user: User,
        businessModelCanvas: BusinessModelCanvas,
    ) {
        val bmcId = businessModelCanvas.id!!

        coroutineScope.launch {
            try {
                taskManager.getOrCreateTask(bmcId) {
                    competitorAnalysisService.performAnalysisInternal(user, businessModelCanvas)
                }
            } catch (e: Exception) {
                logger.error("경쟁사 분석 실패 - BMC ID: {}", bmcId, e)
            }
        }
    }
}
