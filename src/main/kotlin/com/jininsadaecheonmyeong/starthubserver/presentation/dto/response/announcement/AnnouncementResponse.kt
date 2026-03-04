package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementSource

data class AnnouncementResponse(
    val id: Long,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val likeCount: Int,
    val supportField: String,
    val targetAge: String,
    val region: String,
    val organizationType: String,
    val startupHistory: String,
    val content: String,
    val isLiked: Boolean? = null,
    val isNatural: Boolean? = null,
    val source: AnnouncementSource? = null,
    val originalFileUrls: List<String>? = null,
    val pdfFileUrls: List<String>? = null,
) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun from(
            announcement: Announcement,
            isLiked: Boolean? = null,
            isNatural: Boolean? = null,
        ) = AnnouncementResponse(
            id = announcement.id!!,
            title = announcement.title,
            url = announcement.url,
            organization = announcement.organization,
            receptionPeriod = announcement.receptionPeriod,
            likeCount = announcement.likeCount,
            supportField = announcement.supportField,
            targetAge = announcement.targetAge,
            region = announcement.region,
            organizationType = announcement.organizationType,
            startupHistory = announcement.startupHistory,
            content = announcement.content,
            isLiked = isLiked,
            isNatural = isNatural,
            source = announcement.source,
            originalFileUrls = announcement.originalFileUrls?.let { parseJsonArray(it) },
            pdfFileUrls = announcement.pdfFileUrls?.let { parseJsonArray(it) },
        )

        private fun parseJsonArray(json: String): List<String>? {
            return try {
                objectMapper.readValue(json, object : TypeReference<List<String>>() {})
            } catch (_: Exception) {
                null
            }
        }
    }
}
