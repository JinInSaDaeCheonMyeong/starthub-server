package com.jininsadaecheonmyeong.starthubserver.dto.response.announcement

import com.jininsadaecheonmyeong.starthubserver.entity.announcement.Announcement

data class AnnouncementResponse(
    val id: Long,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
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
    val isNatural: Boolean? = null,
) {
    companion object {
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
            contactNumber = announcement.contactNumber,
            region = announcement.region,
            organizationType = announcement.organizationType,
            startupHistory = announcement.startupHistory,
            departmentInCharge = announcement.departmentInCharge,
            content = announcement.content,
            isLiked = isLiked,
            isNatural = isNatural,
        )
    }
}
