package com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement

data class AnnouncementResponse(
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
) {
    companion object {
        fun from(announcement: Announcement) =
            AnnouncementResponse(
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
            )
    }
}
