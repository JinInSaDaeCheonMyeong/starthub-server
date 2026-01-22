package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserStartupFieldRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserContextService(
    private val bmcRepository: BusinessModelCanvasRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
) {
    fun buildContext(user: User): UserContext {
        val bmcs = getBmcSummaries(user)
        val interests = getInterests(user)
        val likedAnnouncements = getLikedAnnouncements(user)

        return UserContext(
            userId = user.id!!,
            username = user.username,
            companyName = user.companyName,
            companyDescription = user.companyDescription,
            startupStatus = user.startupStatus?.name,
            startupLocation = user.startupLocation,
            startupHistory = user.startupHistory,
            bmcs = bmcs,
            interests = interests,
            likedAnnouncements = likedAnnouncements,
        )
    }

    fun buildContextString(user: User): String {
        val context = buildContext(user)
        return buildString {
            appendLine("## 사용자 정보")
            context.username?.let { appendLine("- 이름: $it") }
            context.companyName?.let { appendLine("- 회사명: $it") }
            context.companyDescription?.let { appendLine("- 회사 설명: $it") }
            context.startupStatus?.let { appendLine("- 창업 상태: $it") }
            context.startupLocation?.let { appendLine("- 창업 지역: $it") }
            context.startupHistory?.let { appendLine("- 창업 경력: ${it}년") }

            if (context.interests.isNotEmpty()) {
                appendLine()
                appendLine("## 관심 분야")
                context.interests.forEach { appendLine("- $it") }
            }

            if (context.bmcs.isNotEmpty()) {
                appendLine()
                appendLine("## 비즈니스 모델 캔버스")
                context.bmcs.forEachIndexed { index, bmc ->
                    appendLine("### BMC ${index + 1}: ${bmc.title}")
                    bmc.valueProposition?.let { appendLine("- 가치 제안: $it") }
                    bmc.customerSegments?.let { appendLine("- 목표 고객: $it") }
                    bmc.keyActivities?.let { appendLine("- 핵심 활동: $it") }
                    bmc.revenueStreams?.let { appendLine("- 수익 모델: $it") }
                }
            }

            if (context.likedAnnouncements.isNotEmpty()) {
                appendLine()
                appendLine("## 관심 있는 공고 (최근 ${context.likedAnnouncements.size}개)")
                context.likedAnnouncements.forEach { announcement ->
                    appendLine("- ${announcement.title} (${announcement.organization})")
                }
            }
        }
    }

    private fun getBmcSummaries(user: User): List<BmcSummary> {
        val bmcs = bmcRepository.findAllByUserAndDeletedFalse(user)
        return bmcs.filter { it.isCompleted }.map { bmc ->
            BmcSummary(
                id = bmc.id!!,
                title = bmc.title,
                valueProposition = bmc.valueProposition,
                customerSegments = bmc.customerSegments,
                keyActivities = bmc.keyActivities,
                revenueStreams = bmc.revenueStreams,
            )
        }
    }

    private fun getInterests(user: User): List<String> {
        val startupFields = userStartupFieldRepository.findByUser(user)
        return startupFields.map { field ->
            field.customField ?: field.businessType.name
        }
    }

    private fun getLikedAnnouncements(
        user: User,
        limit: Int = 10,
    ): List<LikedAnnouncementSummary> {
        val likes =
            announcementLikeRepository.findByUserOrderByCreatedAtDesc(
                user,
                PageRequest.of(0, limit),
            )

        return likes.content.map { like ->
            LikedAnnouncementSummary(
                id = like.announcement.id!!,
                title = like.announcement.title,
                organization = like.announcement.organization,
                url = like.announcement.url,
            )
        }
    }
}

data class UserContext(
    val userId: Long,
    val username: String?,
    val companyName: String?,
    val companyDescription: String?,
    val startupStatus: String?,
    val startupLocation: String?,
    val startupHistory: Int?,
    val bmcs: List<BmcSummary>,
    val interests: List<String>,
    val likedAnnouncements: List<LikedAnnouncementSummary>,
)

data class BmcSummary(
    val id: Long,
    val title: String,
    val valueProposition: String?,
    val customerSegments: String?,
    val keyActivities: String?,
    val revenueStreams: String?,
)

data class LikedAnnouncementSummary(
    val id: Long,
    val title: String,
    val organization: String,
    val url: String,
)
