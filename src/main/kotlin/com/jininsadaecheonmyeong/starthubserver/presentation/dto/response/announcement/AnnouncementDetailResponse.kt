package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementSource
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementStatus

data class FileInfo(
    val url: String,
    val name: String,
)

data class AnnouncementDetailResponse(
    val id: Long,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val status: AnnouncementStatus,
    val likeCount: Int,
    val supportField: String,
    val targetAge: String,
    val region: String,
    val organizationType: String,
    val startupHistory: String,
    val content: String,
    val isLiked: Boolean? = null,
    val source: AnnouncementSource? = null,
    val originalFiles: List<FileInfo>? = null,
    val pdfFiles: List<FileInfo>? = null,
) {
    companion object {
        private val objectMapper = ObjectMapper().registerKotlinModule()

        fun from(
            announcement: Announcement,
            isLiked: Boolean? = null,
        ) = AnnouncementDetailResponse(
            id = announcement.id!!,
            title = announcement.title,
            url = announcement.url,
            organization = announcement.organization,
            receptionPeriod = announcement.receptionPeriod,
            status = announcement.status,
            likeCount = announcement.likeCount,
            supportField = announcement.supportField,
            targetAge = announcement.targetAge,
            region = announcement.region,
            organizationType = announcement.organizationType,
            startupHistory = announcement.startupHistory,
            content = announcement.content,
            isLiked = isLiked,
            source = announcement.source,
            originalFiles = announcement.originalFileUrls?.let { parseFileInfoArray(it) },
            pdfFiles = announcement.pdfFileUrls?.let { parseFileInfoArray(it) },
        )

        private fun parseFileInfoArray(json: String): List<FileInfo>? {
            return try {
                objectMapper.readValue(json, object : TypeReference<List<FileInfo>>() {})
            } catch (_: Exception) {
                try {
                    val urls = objectMapper.readValue(json, object : TypeReference<List<String>>() {})
                    urls.map { url ->
                        val name = url.substringAfterLast("/").substringBefore("?")
                        FileInfo(url = url, name = name)
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
}
