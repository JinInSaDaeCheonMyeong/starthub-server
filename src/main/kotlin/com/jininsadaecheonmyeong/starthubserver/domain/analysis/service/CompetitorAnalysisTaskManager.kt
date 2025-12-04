package com.jininsadaecheonmyeong.starthubserver.domain.analysis.service

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.data.response.CompetitorAnalysisResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CompetitorAnalysisTaskManager {
    private val ongoingTasks = ConcurrentHashMap<Long, Deferred<CompetitorAnalysisResponse>>()

    suspend fun getOrCreateTask(
        bmcId: Long,
        taskSupplier: suspend () -> CompetitorAnalysisResponse,
    ): CompetitorAnalysisResponse {
        val existingTask = ongoingTasks[bmcId]
        if (existingTask != null) {
            return existingTask.await()
        }

        val deferred = CompletableDeferred<CompetitorAnalysisResponse>()
        val previousTask = ongoingTasks.putIfAbsent(bmcId, deferred)

        if (previousTask != null) {
            return previousTask.await()
        }

        return try {
            val result = taskSupplier()
            deferred.complete(result)
            result
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            ongoingTasks.remove(bmcId)
        }
    }

    fun getOngoingTask(bmcId: Long): Deferred<CompetitorAnalysisResponse>? = ongoingTasks[bmcId]

    fun isTaskRunning(bmcId: Long): Boolean = ongoingTasks.containsKey(bmcId)

    fun removeTask(bmcId: Long) {
        ongoingTasks.remove(bmcId)
    }
}
