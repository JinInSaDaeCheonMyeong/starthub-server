package com.jininsadaecheonmyeong.starthubserver.domain.analysis.service

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Component
class CompetitorAnalysisTaskManager {
    private val ongoingTasks = ConcurrentHashMap<Long, CompletableFuture<CompetitorAnalysisResponse>>()

    fun getOrCreateTask(
        bmcId: Long,
        taskSupplier: () -> CompletableFuture<CompetitorAnalysisResponse>,
    ): CompletableFuture<CompetitorAnalysisResponse> {
        return ongoingTasks.computeIfAbsent(bmcId) {
            val future = taskSupplier()
            future.whenComplete { _, _ ->
                ongoingTasks.remove(bmcId)
            }
            future
        }
    }

    fun getOngoingTask(bmcId: Long): CompletableFuture<CompetitorAnalysisResponse>? = ongoingTasks[bmcId]

    fun isTaskRunning(bmcId: Long): Boolean = ongoingTasks.containsKey(bmcId)

    fun removeTask(bmcId: Long) {
        ongoingTasks.remove(bmcId)
    }
}
