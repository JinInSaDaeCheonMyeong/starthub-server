package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.BmcModificationType
import java.time.LocalDateTime
import java.util.UUID

data class BmcModificationResponse(
    val id: UUID,
    val bmcId: UUID,
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
