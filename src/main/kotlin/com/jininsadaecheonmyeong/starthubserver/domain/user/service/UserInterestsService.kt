package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.*
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserInterestRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserInterestsService(
    private val userRepository: UserRepository,
    private val userInterestRepository: UserInterestRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository
) {
    
    fun getUserInterestsData(userId: Long): UserInterestsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        val userProfile = UserProfileDto(
            userId = user.id!!,
            username = user.username,
            introduction = user.introduction
        )
        
        val userInterests = userInterestRepository.findByUserId(userId).map { interest ->
            UserInterestDto(
                userId = userId,
                businessType = interest.businessType
            )
        }
        
        val likedAnnouncements = announcementLikeRepository.findByUserId(userId).map { like ->
            LikedAnnouncementDto(
                userId = userId,
                announcement = AnnouncementDto(
                    id = like.announcement.id!!,
                    title = like.announcement.title,
                    organization = like.announcement.organization,
                    supportField = like.announcement.supportField,
                    targetAge = like.announcement.targetAge,
                    region = like.announcement.region,
                    content = like.announcement.content
                ),
                likedAt = like.createdAt
            )
        }
        
        val bmcData = businessModelCanvasRepository.findByUserIdAndDeletedFalse(userId).map { bmc ->
            BMCDto(
                id = bmc.id!!,
                userId = userId,
                title = bmc.title,
                keyPartners = bmc.keyPartners,
                keyActivities = bmc.keyActivities,
                keyResources = bmc.keyResources,
                valueProposition = bmc.valueProposition,
                customerRelationships = bmc.customerRelationships,
                channels = bmc.channels,
                customerSegments = bmc.customerSegments,
                costStructure = bmc.costStructure,
                revenueStreams = bmc.revenueStreams,
                isCompleted = bmc.isCompleted
            )
        }
        
        return UserInterestsResponse(
            userProfile = userProfile,
            userInterests = userInterests,
            likedAnnouncements = likedAnnouncements,
            bmcData = bmcData
        )
    }
    
    fun getAllUsersInterestsData(): List<UserInterestsResponse> {
        val allUsers = userRepository.findByDeletedFalse()
        return allUsers.map { user ->
            getUserInterestsData(user.id!!)
        }
    }
}