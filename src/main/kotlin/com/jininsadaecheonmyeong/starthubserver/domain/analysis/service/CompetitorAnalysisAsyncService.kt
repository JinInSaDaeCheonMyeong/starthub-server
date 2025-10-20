package com.jininsadaecheonmyeong.starthubserver.domain.analysis.service

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class CompetitorAnalysisAsyncService(
    private val competitorAnalysisService: CompetitorAnalysisService,
    private val taskManager: CompetitorAnalysisTaskManager,
) {
    private val logger = logger()

    @Async("taskExecutor")
    fun performAnalysisAsync(
        user: User,
        businessModelCanvas: BusinessModelCanvas,
    ): CompletableFuture<CompetitorAnalysisResponse> {
        val bmcId = businessModelCanvas.id!!

        return taskManager.getOrCreateTask(bmcId) {
            CompletableFuture.supplyAsync {
                try {
                    competitorAnalysisService.performAnalysisInternal(user, businessModelCanvas)
                } catch (e: Exception) {
                    logger.error("Failed async competitor analysis for BMC ID: {}", bmcId, e)
                    throw e
                }
            }
        }
    }
}
