package com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import java.time.LocalDateTime

data class Announcement(
    val id: Long? = null,
    val title: String,
    val url: String,
    val organization: String,
    val receptionPeriod: String,
    val status: AnnouncementStatus = AnnouncementStatus.ACTIVE,
    val likeCount: Int = 0,
    val supportField: String,
    val targetAge: String?,
    val contactNumber: String?,
    val region: String?,
    val organizationType: String?,
    val startupHistory: String?,
    val departmentInCharge: String?,
    val content: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(title.isNotBlank()) { "제목은 필수입니다" }
        require(url.isNotBlank()) { "URL은 필수입니다" }
        require(organization.isNotBlank()) { "기관명은 필수입니다" }
        require(receptionPeriod.isNotBlank()) { "접수기간은 필수입니다" }
        require(likeCount >= 0) { "좋아요 수는 0 이상이어야 합니다" }
    }

    fun incrementLikeCount(): Announcement {
        return copy(likeCount = likeCount + 1)
    }

    fun decrementLikeCount(): Announcement {
        return copy(likeCount = maxOf(0, likeCount - 1))
    }

    fun deactivate(): Announcement {
        return copy(status = AnnouncementStatus.INACTIVE)
    }

    fun isActive(): Boolean = status == AnnouncementStatus.ACTIVE

    companion object {
        fun create(
            title: String,
            url: String,
            organization: String,
            receptionPeriod: String,
            supportField: String,
            targetAge: String? = null,
            contactNumber: String? = null,
            region: String? = null,
            organizationType: String? = null,
            startupHistory: String? = null,
            departmentInCharge: String? = null,
            content: String
        ): Announcement {
            return Announcement(
                title = title,
                url = url,
                organization = organization,
                receptionPeriod = receptionPeriod,
                supportField = supportField,
                targetAge = targetAge,
                contactNumber = contactNumber,
                region = region,
                organizationType = organizationType,
                startupHistory = startupHistory,
                departmentInCharge = departmentInCharge,
                content = content
            )
        }
    }
}
