package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
) {
    companion object {
        private const val K_STARTUP_URL = "https://www.k-startup.go.kr/web/contents/bizpbanc-ongoing.do"
        private const val BASE_URL = "https://www.k-startup.go.kr"
    }

    @Transactional
    fun scrapeAndSaveAnnouncements() {
        var page = 1
        var consecutivePagesWithNoNewSaves = 0
        val consecutivePageLimit = 1

        while (true) {
            if (consecutivePagesWithNoNewSaves >= consecutivePageLimit) break

            try {
                val urlWithPage = "$K_STARTUP_URL?page=$page"
                val doc = Jsoup.connect(urlWithPage).get()
                val announcements = doc.select("div.board_list-wrap .notice")

                if (announcements.isEmpty()) break

                var newAnnouncementsOnPage = 0

                for (element in announcements) {
                    if (element.text().contains("등록된 데이터가 없습니다.")) continue

                    val titleElement = element.selectFirst("p.tit")
                    val linkElement = element.selectFirst(".middle a")
                    val infoElements = element.select("div.bottom span.list")

                    if (titleElement != null && linkElement != null && infoElements.size >= 5) {
                        val title = titleElement.text()
                        val href = linkElement.attr("href")
                        val announcementId = href.filter { it.isDigit() }

                        if (announcementId.isNotBlank()) {
                            val absoluteUrl = "$BASE_URL/web/contents/bizpbanc-view.do?pbancSn=$announcementId"

                            if (!repository.existsByUrl(absoluteUrl)) {
                                val organization = infoElements[1].text()
                                val startDate = infoElements[3].text().replace("시작일자", "").trim()
                                val endDate = infoElements[4].text().replace("마감일자", "").trim()
                                val receptionPeriod = "$startDate ~ $endDate"

                                val announcement =
                                    Announcement(
                                        title = title,
                                        url = absoluteUrl,
                                        organization = organization,
                                        receptionPeriod = receptionPeriod,
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

    fun findAllAnnouncements(userId: Long?, pageable: Pageable): Page<AnnouncementResponse> {
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE, pageable)
        val user = userId?.let { userRepository.findById(it).orElse(null) }

        return announcements.map { announcement ->
            val isLiked = user?.let { announcementLikeRepository.existsByUserAndAnnouncement(it, announcement) } ?: false
            AnnouncementResponse.from(announcement, isLiked)
        }
    }

    @Transactional
    fun softDeleteExpiredAnnouncements() {
        val announcements = repository.findAllByStatus(AnnouncementStatus.ACTIVE)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        announcements.forEach { announcement ->
            val endDateString = announcement.receptionPeriod.split("~")[1].trim()
            val endDate = LocalDate.parse(endDateString, formatter)
            if (endDate.isBefore(today)) {
                announcement.status = AnnouncementStatus.INACTIVE
                repository.save(announcement)
            }
        }
    }
}
