package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import java.time.LocalDateTime

data class BusinessModelCanvasResponse(
    val id: Long,
    val title: String,
    val customerSegments: String?,
    val valueProposition: String?,
    val channels: String?,
    val customerRelationships: String?,
    val revenueStreams: String?,
    val keyResources: String?,
    val keyActivities: String?,
    val keyPartners: String?,
    val costStructure: String?,
    val isCompleted: Boolean,
    val imageUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(bmc: BusinessModelCanvas): BusinessModelCanvasResponse {
            return BusinessModelCanvasResponse(
                id = bmc.id!!,
                title = bmc.title,
                customerSegments = bmc.customerSegments,
                valueProposition = bmc.valueProposition,
                channels = bmc.channels,
                customerRelationships = bmc.customerRelationships,
                revenueStreams = bmc.revenueStreams,
                keyResources = bmc.keyResources,
                keyActivities = bmc.keyActivities,
                keyPartners = bmc.keyPartners,
                costStructure = bmc.costStructure,
                isCompleted = bmc.isCompleted,
                imageUrl = bmc.imageUrl,
                createdAt = bmc.createdAt,
                updatedAt = bmc.updatedAt,
            )
        }
    }
}
