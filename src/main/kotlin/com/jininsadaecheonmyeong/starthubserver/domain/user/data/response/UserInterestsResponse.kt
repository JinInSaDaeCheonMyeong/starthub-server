package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import java.time.LocalDateTime

data class UserInterestsResponse(
    val userProfile: UserProfileDto,
    val userInterests: List<UserInterestDto>,
    val likedAnnouncements: List<LikedAnnouncementDto>,
    val bmcData: List<BMCDto>
)

data class UserProfileDto(
    val userId: Long,
    val username: String?,
    val introduction: String?
)

data class UserInterestDto(
    val userId: Long,
    val businessType: BusinessType
)

data class LikedAnnouncementDto(
    val userId: Long,
    val announcement: AnnouncementDto,
    val likedAt: LocalDateTime
)

data class AnnouncementDto(
    val id: Long,
    val title: String,
    val organization: String,
    val supportField: String?,
    val targetAge: String?,
    val region: String?,
    val content: String
)

data class BMCDto(
    val id: Long,
    val userId: Long,
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
    val isCompleted: Boolean
)