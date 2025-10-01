package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AnonymousBmcService(
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
) {
    fun getAnonymousCompletedBmcs(limit: Int = 50): List<AnonymousBmcData> {
        return businessModelCanvasRepository.findAllByDeletedFalse()
            .filter { it.isCompleted }
            .shuffled()
            .take(limit)
            .map { bmc ->
                AnonymousBmcData(
                    valueProposition = bmc.valueProposition,
                    customerSegments = bmc.customerSegments,
                    channels = bmc.channels,
                    customerRelationships = bmc.customerRelationships,
                    revenueStreams = bmc.revenueStreams,
                    keyResources = bmc.keyResources,
                    keyActivities = bmc.keyActivities,
                    keyPartners = bmc.keyPartners,
                    costStructure = bmc.costStructure,
                )
            }
    }

    fun findSimilarBmcs(
        targetBmc: AnonymousBmcData,
        limit: Int = 10,
    ): List<AnonymousBmcData> {
        val allBmcs = getAnonymousCompletedBmcs(100)

        return allBmcs.asSequence()
            .map { bmc ->
                bmc to calculateSimilarity(targetBmc, bmc)
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }

    private fun calculateSimilarity(
        bmc1: AnonymousBmcData,
        bmc2: AnonymousBmcData,
    ): Double {
        val fields =
            listOf(
                bmc1.valueProposition to bmc2.valueProposition,
                bmc1.customerSegments to bmc2.customerSegments,
                bmc1.channels to bmc2.channels,
                bmc1.revenueStreams to bmc2.revenueStreams,
                bmc1.keyActivities to bmc2.keyActivities,
            )

        return fields.mapNotNull { (field1, field2) ->
            if (field1.isNullOrBlank() || field2.isNullOrBlank()) {
                null
            } else {
                calculateTextSimilarity(field1, field2)
            }
        }.average()
    }

    private fun calculateTextSimilarity(
        text1: String,
        text2: String,
    ): Double {
        val words1 = text1.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        val words2 = text2.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return intersection.toDouble() / union.toDouble()
    }
}

data class AnonymousBmcData(
    val valueProposition: String?,
    val customerSegments: String?,
    val channels: String?,
    val customerRelationships: String?,
    val revenueStreams: String?,
    val keyResources: String?,
    val keyActivities: String?,
    val keyPartners: String?,
    val costStructure: String?,
)
