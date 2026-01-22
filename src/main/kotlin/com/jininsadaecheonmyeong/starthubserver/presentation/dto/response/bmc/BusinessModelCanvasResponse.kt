package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.enums.bmc.BmcTemplateType
import java.time.LocalDateTime

data class BusinessModelCanvasResponse(
    val id: Long,
    val title: String,
    val templateType: BmcTemplateType,
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
                templateType = bmc.templateType,
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
