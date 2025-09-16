package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementSimpleResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.jsoup.Jsoup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class AnnouncementService(
    private val repository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
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

    fun findAllAnnouncements(pageable: Pageable): Page<AnnouncementResponse> {
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE, pageable)

        return announcements.map { announcement ->
            AnnouncementResponse.from(announcement)
        }
    }

    fun getAllAnnouncementsWithLikeStatus(pageable: Pageable): Page<AnnouncementSimpleResponse> {
        val user = UserAuthenticationHolder.current()
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE, pageable)
        val announcementList = announcements.content

        val userLikes = announcementLikeRepository.findAllByUserAndAnnouncementIn(user, announcementList)
        val likedAnnouncementIds = userLikes.map { it.announcement.id }.toSet()

        return announcements.map { announcement ->
            AnnouncementSimpleResponse.from(
                announcement = announcement,
                isLiked = likedAnnouncementIds.contains(announcement.id),
            )
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
        val user = UserAuthenticationHolder.current()
        val likedAnnouncements = announcementLikeRepository.findByUserOrderByCreatedAtDesc(user, pageable)

        return likedAnnouncements.map { AnnouncementResponse.from(it.announcement) }
    }

    fun getAnnouncementDetail(announcementId: Long): AnnouncementDetailResponse {
        val announcement =
            repository.findById(announcementId).orElseThrow { AnnouncementNotFoundException("찾을 수 없는 공고") }
        return AnnouncementDetailResponse(announcement)
    }

    fun searchAnnouncements(
        title: String?,
        supportField: String?,
        targetRegion: String?,
        targetGroup: String?,
        targetAge: String?,
        businessExperience: String?,
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

        return announcements.map { announcement ->
            AnnouncementResponse.from(announcement)
        }
    }
}
