package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.repository.analysis.CompetitorAnalysisRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.schedule.ScheduleRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserStartupFieldRepository
import com.jininsadaecheonmyeong.starthubserver.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class UserContextService(
    private val bmcRepository: BusinessModelCanvasRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val competitorAnalysisRepository: CompetitorAnalysisRepository,
    private val scheduleRepository: ScheduleRepository,
    private val chatbotRAGClient: ChatbotRAGClient,
) {
    private val log = logger()
    private val embeddingScope = CoroutineScope(Dispatchers.IO)

    fun embedUserContextAsync(user: User) {
        embeddingScope.launch {
            try {
                val context = buildContext(user)
                val bmcEmbedDataList = getBmcEmbedDataList(user)
                val competitorAnalysisEmbedDataList = getCompetitorAnalysisEmbedDataList(user)

                val request =
                    EmbedUserContextRequest(
                        userId = context.userId,
                        bmcs = bmcEmbedDataList,
                        interests = context.interests,
                        likedAnnouncementUrls = context.likedAnnouncements.map { it.url },
                        competitorAnalyses = competitorAnalysisEmbedDataList.ifEmpty { null },
                    )

                chatbotRAGClient.embedUserContext(request)
            } catch (e: Exception) {
                log.error("사용자 컨텍스트 임베딩 중 오류: userId=${user.id}, error=${e.message}")
            }
        }
    }

    private fun getBmcEmbedDataList(user: User): List<BmcEmbedData> {
        val bmcs = bmcRepository.findAllByUserAndDeletedFalse(user)
        return bmcs.filter { it.isCompleted }.map { bmc ->
            BmcEmbedData(
                id = bmc.id!!,
                title = bmc.title,
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

    private fun getCompetitorAnalysisEmbedDataList(user: User): List<CompetitorAnalysisEmbedData> {
        val analyses = competitorAnalysisRepository.findAllByUserAndDeletedFalse(user)
        return analyses.map { analysis ->
            CompetitorAnalysisEmbedData(
                id = analysis.id!!,
                bmcId = analysis.businessModelCanvas.id!!,
                bmcTitle = analysis.businessModelCanvas.title,
                userBmcSummary = analysis.userBmcSummary?.take(500),
                strengths = analysis.strengthsAnalysis?.take(500),
                weaknesses = analysis.weaknessesAnalysis?.take(500),
                globalStrategy = analysis.globalExpansionStrategy?.take(500),
            )
        }
    }

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
                    appendLine("### BMC ${index + 1}: ${bmc.title} (ID: ${bmc.id})")
                    bmc.valueProposition?.let { appendLine("- 가치 제안: $it") }
                    bmc.customerSegments?.let { appendLine("- 목표 고객: $it") }
                    bmc.keyActivities?.let { appendLine("- 핵심 활동: $it") }
                    bmc.revenueStreams?.let { appendLine("- 수익 모델: $it") }
                }
            }

            if (context.likedAnnouncements.isNotEmpty()) {
                appendLine()
                appendLine("## 관심 있는 공고 (최근 ${context.likedAnnouncements.size}개)")
                appendLine("오늘 날짜: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                context.likedAnnouncements.forEach { announcement ->
                    appendLine("- ${announcement.title}")
                    appendLine("  기관: ${announcement.organization}, 접수기간: ${announcement.receptionPeriod}")
                    appendLine("  [ID: ${announcement.id}, URL: ${announcement.url}]")
                }
            }
        }
    }

    fun buildContextStringWithAnalysis(user: User): String {
        val contextString = buildContextString(user)
        val analyses = competitorAnalysisRepository.findAllByUserAndDeletedFalse(user)
        val schedules = getUpcomingSchedules(user)

        return buildString {
            append(contextString)

            if (analyses.isNotEmpty()) {
                appendLine()
                appendLine("## 경쟁사분석")
                analyses.forEach { analysis ->
                    appendLine(
                        "### ${analysis.businessModelCanvas.title} 경쟁사분석 (ID: ${analysis.id}, BMC ID: ${analysis.businessModelCanvas.id})",
                    )
                    analysis.userBmcSummary?.let { appendLine("- BMC 요약: ${it.take(200)}") }
                    analysis.strengthsAnalysis?.let { appendLine("- 강점: ${it.take(200)}") }
                    analysis.weaknessesAnalysis?.let { appendLine("- 약점: ${it.take(200)}") }
                }
            }

            if (schedules.isNotEmpty()) {
                appendLine()
                appendLine("## 일정에 추가한 공고 (마감일 기준 정렬)")
                appendLine("오늘 날짜: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                schedules.forEach { schedule ->
                    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), schedule.endDate)
                    val urgency =
                        when {
                            daysLeft < 0 -> "(마감됨)"
                            daysLeft == 0L -> "(오늘 마감!)"
                            daysLeft <= 3 -> "(${daysLeft}일 남음 - 긴급)"
                            daysLeft <= 7 -> "(${daysLeft}일 남음)"
                            else -> "(${daysLeft}일 남음)"
                        }
                    appendLine("- ${schedule.title} $urgency [ID: ${schedule.announcementId}, URL: ${schedule.url}]")
                    appendLine("  기관: ${schedule.organization}, 마감일: ${schedule.endDate}")
                }
            }
        }
    }

    private fun getUpcomingSchedules(user: User): List<ScheduleSummary> {
        val today = LocalDate.now()
        val schedules = scheduleRepository.findUpcomingSchedulesWithAnnouncement(user, today.minusDays(7))
        return schedules.take(20).map { schedule ->
            ScheduleSummary(
                id = schedule.id!!,
                announcementId = schedule.announcement.id!!,
                title = schedule.announcement.title,
                organization = schedule.announcement.organization,
                url = schedule.announcement.url,
                startDate = schedule.startDate,
                endDate = schedule.endDate,
            )
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
                receptionPeriod = like.announcement.receptionPeriod,
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
    val receptionPeriod: String,
)

data class ScheduleSummary(
    val id: Long,
    val announcementId: Long,
    val title: String,
    val organization: String,
    val url: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
