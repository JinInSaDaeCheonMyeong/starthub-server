package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc

import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.BmcModificationType
import java.time.LocalDateTime

data class BmcModificationResponse(
    val id: Long,
    val bmcId: Long,
    val modificationRequest: String,
    val requestType: BmcModificationType,
    val isProcessed: Boolean,
    val aiResponse: String?,
    val createdAt: LocalDateTime,
    val updatedBmc: BusinessModelCanvasResponse?,
) {
    companion object {
        fun from(
            modificationRequest: BmcModificationRequest,
            updatedBmc: BusinessModelCanvasResponse? = null,
        ): BmcModificationResponse {
            return BmcModificationResponse(
                id = modificationRequest.id!!,
                bmcId = modificationRequest.businessModelCanvas.id!!,
                modificationRequest = modificationRequest.modificationRequest,
                requestType = modificationRequest.requestType,
                isProcessed = modificationRequest.isProcessed,
                aiResponse = modificationRequest.aiResponse,
                createdAt = modificationRequest.createdAt,
                updatedBmc = updatedBmc,
            )
        }
    }
}
