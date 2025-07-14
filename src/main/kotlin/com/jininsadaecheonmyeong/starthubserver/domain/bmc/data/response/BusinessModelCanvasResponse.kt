package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import java.time.LocalDateTime

data class BusinessModelCanvasResponse(
    val id: Long,
    val title: String,
    val keyPartners: String?,
    val keyActivities: String?,
    val keyResources: String?,
    val valueProposition: String?,
    val customerRelationships: String?,
    val channels: String?,
    val customerSegments: String?,
    val costStructure: String?,
    val revenueStreams: String?,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(bmc: BusinessModelCanvas): BusinessModelCanvasResponse {
            return BusinessModelCanvasResponse(
                id = bmc.id!!,
                title = bmc.title,
                keyPartners = bmc.keyPartners,
                keyActivities = bmc.keyActivities,
                keyResources = bmc.keyResources,
                valueProposition = bmc.valueProposition,
                customerRelationships = bmc.customerRelationships,
                channels = bmc.channels,
                customerSegments = bmc.customerSegments,
                costStructure = bmc.costStructure,
                revenueStreams = bmc.revenueStreams,
                isCompleted = bmc.isCompleted,
                createdAt = bmc.createdAt,
                updatedAt = bmc.updatedAt,
            )
        }
    }
}
