package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement

import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementStatus

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
    val contactNumber: String,
    val region: String,
    val organizationType: String,
    val startupHistory: String,
    val departmentInCharge: String,
    val content: String,
    val isLiked: Boolean? = null,
) {
    companion object {
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
            contactNumber = announcement.contactNumber,
            region = announcement.region,
            organizationType = announcement.organizationType,
            startupHistory = announcement.startupHistory,
            departmentInCharge = announcement.departmentInCharge,
            content = announcement.content,
            isLiked = isLiked,
        )
    }
}
