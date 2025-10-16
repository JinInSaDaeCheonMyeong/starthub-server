package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request.BmcInfo
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request.LikedAnnouncementUrl
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request.LikedAnnouncementsContent
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request.RecommendationRequest
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.RecommendationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.RecommendedAnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserInterestNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserStartupFieldRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class AnnouncementService(
    private val repository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val webClient: WebClient,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    @param:Value("\${recommendation.fastapi-url}") private val fastapiUrl: String,
) {
    companion object {
        private const val K_STARTUP_URL = "https://www.k-startup.go.kr/web/contents/bizpbanc-ongoing.do"
    }

    @Transactional
    fun scrapeAndSaveAnnouncements() {
        var page = 1
        var consecutivePagesWithNoNewSaves = 0
        val consecutivePageLimit = 1

        while (true) {
            if (consecutivePagesWithNoNewSaves >= consecutivePageLimit) break

            try {
                val listUrl = "$K_STARTUP_URL?page=$page"
                val listDoc = Jsoup.connect(listUrl).get()
                val announcements = listDoc.select("div.board_list-wrap .notice")

                if (announcements.isEmpty()) break

                var newAnnouncementsOnPage = 0

                for (element in announcements) {
                    if (element.text().contains("등록된 데이터가 없습니다.")) continue

                    val titleElement = element.selectFirst("p.tit")
                    val linkElement = element.selectFirst(".middle a")

                    if (titleElement != null && linkElement != null) {
                        val href = linkElement.attr("href")
                        val announcementId = href.filter { it.isDigit() }

                        if (announcementId.isNotBlank()) {
                            val detailUrl = "$K_STARTUP_URL?schM=view&pbancSn=$announcementId"

                            if (!repository.existsByUrl(detailUrl)) {
                                val detailDoc = Jsoup.connect(detailUrl).get()

                                fun extractText(selector: String): String? {
                                    return detailDoc.selectFirst(selector)?.text()?.trim()
                                }

                                val title = detailDoc.selectFirst("div.title h3")?.text() ?: titleElement.text()
                                val organization = extractText("li.dot_list:has(p.tit:contains(기관명)) p.txt") ?: ""
                                val receptionPeriod = extractText("li.dot_list:has(p.tit:contains(접수기간)) p.txt") ?: ""
                                val supportField = extractText("li.dot_list:has(p.tit:contains(지원분야)) p.txt") ?: ""
                                val targetAge = extractText("li.dot_list:has(p.tit:contains(대상연령)) p.txt") ?: ""
                                val contactNumber = extractText("li.dot_list:has(p.tit:contains(연락처)) p.txt") ?: ""
                                val region = extractText("li.dot_list:has(p.tit:contains(지역)) p.txt") ?: ""
                                val organizationType = extractText("li.dot_list:has(p.tit:contains(기관구분)) p.txt") ?: ""
                                val startupHistory = extractText("li.dot_list:has(p.tit:contains(창업업력)) p.txt") ?: ""
                                val departmentInCharge = extractText("li.dot_list:has(p.tit:contains(담당부서)) p.txt") ?: ""
                                val content = detailDoc.selectFirst("div.information_list-wrap")?.html() ?: ""

                                val announcement =
                                    Announcement(
                                        title = title,
                                        url = detailUrl,
                                        organization = organization,
                                        receptionPeriod = receptionPeriod,
                                        supportField = supportField,
                                        targetAge = targetAge,
                                        contactNumber = contactNumber,
                                        region = region,
                                        organizationType = organizationType,
                                        startupHistory = startupHistory,
                                        departmentInCharge = departmentInCharge,
                                        content = content,
                                    )
                                repository.save(announcement)
                                newAnnouncementsOnPage++
                            }
                        }
                    }
                }

                if (newAnnouncementsOnPage == 0) {
                    consecutivePagesWithNoNewSaves++
                } else {
                    consecutivePagesWithNoNewSaves = 0
                }

                page++
            } catch (_: Exception) {
                break
            }
        }
    }

    fun findAllAnnouncements(
        pageable: Pageable,
        includeLikeStatus: Boolean,
    ): Page<AnnouncementResponse> {
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE, pageable)
        return if (includeLikeStatus) {
            mapAnnouncementsToResponseWithLikeStatus(announcements)
        } else {
            announcements.map { AnnouncementResponse.from(it) }
        }
    }

    @Transactional
    fun deactivateExpiredAnnouncements() {
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        announcements.forEach { announcement ->
            var endDateString = announcement.receptionPeriod.split("~")[1].trim()
            if (endDateString.contains(" ")) {
                endDateString = endDateString.split(" ")[0]
            }
            val endDate = LocalDate.parse(endDateString, formatter)
            if (endDate.isBefore(today)) {
                announcement.status = AnnouncementStatus.INACTIVE
                repository.save(announcement)
            }
        }
    }

    fun findLikedAnnouncementsByUser(pageable: Pageable): Page<AnnouncementResponse> {
        val user = userAuthenticationHolder.current()
        val likedAnnouncements = announcementLikeRepository.findByUserOrderByCreatedAtDesc(user, pageable)

        return likedAnnouncements.map { AnnouncementResponse.from(it.announcement, true) }
    }

    fun getAnnouncementDetail(
        announcementId: Long,
        includeLikeStatus: Boolean,
    ): AnnouncementDetailResponse {
        val announcement =
            repository.findByIdOrNull(announcementId) ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        return if (includeLikeStatus) {
            val user = userAuthenticationHolder.current()
            val isLiked = announcementLikeRepository.existsByUserAndAnnouncement(user, announcement)
            AnnouncementDetailResponse.from(announcement, isLiked)
        } else {
            AnnouncementDetailResponse.from(announcement)
        }
    }

    fun searchAnnouncements(
        title: String?,
        supportField: String?,
        targetRegion: String?,
        targetGroup: String?,
        targetAge: String?,
        businessExperience: String?,
        includeLikeStatus: Boolean,
        pageable: Pageable,
    ): Page<AnnouncementResponse> {
        val announcements =
            repository.searchAnnouncements(
                title = title,
                supportField = supportField,
                targetRegion = targetRegion,
                targetGroup = targetGroup,
                targetAge = targetAge,
                businessExperience = businessExperience,
                pageable = pageable,
            )
        return if (includeLikeStatus) {
            mapAnnouncementsToResponseWithLikeStatus(announcements)
        } else {
            announcements.map {
                AnnouncementResponse.from(it)
            }
        }
    }

    fun getRecommendedAnnouncements(): List<RecommendedAnnouncementResponse> {
        val user = userAuthenticationHolder.current()
        val userInterests = userStartupFieldRepository.findByUser(user)
        val interestNames = userInterests.map { it.businessType.displayName }

        val likedAnnouncements = announcementLikeRepository.findByUserOrderByCreatedAtDesc(user, Pageable.unpaged())
        val likedUrls = likedAnnouncements.map { LikedAnnouncementUrl(it.announcement.url) }.toList()
        val likedContent = LikedAnnouncementsContent(content = likedUrls)

        val userBmcs = businessModelCanvasRepository.findTop3ByUserAndDeletedFalseOrderByCreatedAtDesc(user)
        val bmcInfos = userBmcs.map {
            BmcInfo(
                customerSegments = it.customerSegments,
                valueProposition = it.valueProposition,
                channels = it.channels,
                customerRelationships = it.customerRelationships,
                revenueStreams = it.revenueStreams,
                keyResources = it.keyResources,
                keyActivities = it.keyActivities,
                keyPartners = it.keyPartners,
                costStructure = it.costStructure,
            )
        }

        val request =
            RecommendationRequest(
                interests = interestNames,
                likedAnnouncements = likedContent,
                bmcs = bmcInfos,
            )

        val recommendationResponse =
            webClient.post()
                .uri("$fastapiUrl/recommend")
                .header(HttpHeaders.ACCEPT, "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.is4xxClientError }) { response ->
                    response.bodyToMono(Map::class.java).flatMap { errorBody ->
                        val detail = errorBody?.get("detail") as? String ?: "알 수 없는 오류가 발생했습니다."
                        Mono.error(UserInterestNotFoundException(detail))
                    }
                }
                .bodyToMono(RecommendationResponse::class.java)
                .block()

        if (recommendationResponse == null || recommendationResponse.recommendations.isEmpty()) {
            return emptyList()
        }

        val scoreMap = recommendationResponse.recommendations.associate { it.title to it.score }
        val recommendedTitles = recommendationResponse.recommendations.map { it.title }

        val announcements = repository.findAllByTitleIn(recommendedTitles)

        val announcementsByTitle = announcements.associateBy { it.title }
        val sortedAnnouncements = recommendedTitles.mapNotNull { title -> announcementsByTitle[title] }

        val userLikes = announcementLikeRepository.findAllByUserAndAnnouncementIn(user, sortedAnnouncements)
        val likedAnnouncementIds = userLikes.map { it.announcement.id }.toSet()

        return sortedAnnouncements.map { announcement ->
            RecommendedAnnouncementResponse.from(
                announcement = announcement,
                isLiked = likedAnnouncementIds.contains(announcement.id),
                score = scoreMap[announcement.title],
            )
        }
    }

    private fun mapAnnouncementsToResponseWithLikeStatus(announcements: Page<Announcement>): Page<AnnouncementResponse> {
        val user = userAuthenticationHolder.current()
        val announcementList = announcements.content
        val userLikes = announcementLikeRepository.findAllByUserAndAnnouncementIn(user, announcementList)
        val likedAnnouncementIds = userLikes.map { it.announcement.id }.toSet()

        return announcements.map { announcement ->
            AnnouncementResponse.from(
                announcement = announcement,
                isLiked = likedAnnouncementIds.contains(announcement.id),
            )
        }
    }
}
