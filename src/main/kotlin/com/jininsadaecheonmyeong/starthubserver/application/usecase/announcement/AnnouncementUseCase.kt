package com.jininsadaecheonmyeong.starthubserver.application.usecase.announcement

import com.fasterxml.jackson.databind.ObjectMapper
import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementSource
import com.jininsadaecheonmyeong.starthubserver.domain.enums.announcement.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.exception.announcement.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.announcement.LikeAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.announcement.LikeNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.UserInterestNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.announcement.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserStartupFieldRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.storage.GcsStorageService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.infrastructure.conversion.DocumentConversionService
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.BmcInfo
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.LikedAnnouncementUrl
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.LikedAnnouncementsContent
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.NaturalLanguageSearchRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.RecommendationRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.NaturalLanguageSearchResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.RecommendationResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.RecommendedAnnouncementResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@Transactional(readOnly = true)
class AnnouncementUseCase(
    private val repository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val webClient: WebClient,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val documentConversionService: DocumentConversionService,
    private val gcsStorageService: GcsStorageService,
    private val objectMapper: ObjectMapper,
    @param:Value("\${recommendation.fastapi-url}") private val fastapiUrl: String,
    @param:Value("\${SEARCH_SERVER_URL}") private val searchServerUrl: String,
) {
    companion object {
        private const val K_STARTUP_URL = "https://www.k-startup.go.kr/web/contents/bizpbanc-ongoing.do"
        private const val BIZINFO_BASE_URL = "https://www.bizinfo.go.kr"
        private const val BIZINFO_LIST_URL = "$BIZINFO_BASE_URL/web/lay1/bbs/S1T122C128/AS/74/list.do"
        private const val BIZINFO_VIEW_URL = "$BIZINFO_BASE_URL/web/lay1/bbs/S1T122C128/AS/74/view.do"
        private const val TIMEOUT_MS = 30000
        private const val GCS_DIRECTORY = "announcements/bizinfo"
    }

    @Transactional
    fun scrapeAndSaveAnnouncements() {
        scrapeKStartup()
        scrapeBizInfo()
    }

    private fun scrapeKStartup() {
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
                                        source = AnnouncementSource.K_STARTUP,
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

    private fun scrapeBizInfo() {
        var page = 1
        var consecutiveEmptyPages = 0
        val maxEmptyPages = 2

        while (consecutiveEmptyPages < maxEmptyPages) {
            try {
                val newCount = scrapeBizInfoPage(page)
                if (newCount == 0) {
                    consecutiveEmptyPages++
                } else {
                    consecutiveEmptyPages = 0
                }
                page++
            } catch (_: Exception) {
                break
            }
        }
    }

    private fun scrapeBizInfoPage(page: Int): Int {
        val listUrl = "$BIZINFO_LIST_URL?rows=15&cpage=$page"
        val doc =
            Jsoup
                .connect(listUrl)
                .timeout(TIMEOUT_MS)
                .get()

        val announceLinks = doc.select("a[href*=pblancId]")
        var newCount = 0

        for (link in announceLinks) {
            val href = link.attr("href")
            val pblancId = extractPblancId(href) ?: continue
            val detailUrl = "$BIZINFO_VIEW_URL?pblancId=$pblancId"

            if (repository.existsByUrl(detailUrl)) {
                continue
            }

            try {
                val announcement = scrapeBizInfoDetail(detailUrl)
                if (announcement != null) {
                    repository.save(announcement)
                    newCount++
                }
            } catch (_: Exception) {
                continue
            }
        }

        return newCount
    }

    private fun scrapeBizInfoDetail(detailUrl: String): Announcement? {
        val doc =
            Jsoup
                .connect(detailUrl)
                .timeout(TIMEOUT_MS)
                .get()

        val title =
            doc.selectFirst("h2.tit, .view_tit h2, .board_view h2, .tit_area h2, h2")?.text()?.trim()
        if (title.isNullOrBlank()) {
            return null
        }
        val organization = extractBizInfoField(doc, "소관부처", "주관기관") ?: ""
        val receptionPeriod = extractBizInfoField(doc, "신청기간", "접수기간", "모집기간") ?: ""
        val supportField = extractBizInfoField(doc, "지원분야", "사업분야") ?: ""
        val region = extractBizInfoField(doc, "지역", "사업지역") ?: ""
        val content = doc.selectFirst("div.view_cont, .board_view_cont, .cont_box")?.html() ?: ""

        val (originalFileUrls, pdfFileUrls) = processBizInfoAttachments(doc)

        return Announcement(
            title = title,
            url = detailUrl,
            organization = organization,
            receptionPeriod = receptionPeriod,
            supportField = supportField,
            targetAge = "",
            contactNumber = "",
            region = region,
            organizationType = "",
            startupHistory = "",
            departmentInCharge = "",
            content = content,
            source = AnnouncementSource.BIZINFO,
            originalFileUrls =
                if (originalFileUrls.isNotEmpty()) {
                    objectMapper.writeValueAsString(originalFileUrls)
                } else {
                    null
                },
            pdfFileUrls =
                if (pdfFileUrls.isNotEmpty()) {
                    objectMapper.writeValueAsString(pdfFileUrls)
                } else {
                    null
                },
        )
    }

    private fun extractBizInfoField(
        doc: Document,
        vararg labels: String,
    ): String? {
        for (label in labels) {
            // 구조 1: span.s_title + div.txt (bizinfo 메인 구조)
            val spanValue =
                doc
                    .select("span.s_title:contains($label)")
                    .firstOrNull()
                    ?.parent()
                    ?.selectFirst("div.txt")
                    ?.text()
                    ?.trim()
            if (!spanValue.isNullOrBlank()) {
                return spanValue
            }

            // 구조 2: th/dt/strong + td/dd (기존 구조)
            val thValue =
                doc
                    .select("th:contains($label), dt:contains($label), strong:contains($label)")
                    .firstOrNull()
                    ?.parent()
                    ?.selectFirst("td, dd, span, p")
                    ?.text()
                    ?.trim()
            if (!thValue.isNullOrBlank()) {
                return thValue
            }
        }
        return null
    }

    private fun extractPblancId(href: String): String? {
        val regex = "pblancId=([A-Za-z_0-9]+)".toRegex()
        return regex.find(href)?.groupValues?.get(1)
    }

    private fun processBizInfoAttachments(doc: Document): Pair<List<String>, List<String>> {
        val originalUrls = mutableListOf<String>()
        val pdfUrls = mutableListOf<String>()

        val attachmentLinks =
            doc.select(
                "a[href*=FileDown], a[href*=fileDown], a[href*=getImageFile], a[onclick*=download]",
            )

        for (link in attachmentLinks) {
            val href = link.attr("href")
            val onclick = link.attr("onclick")
            val fileName = link.text().trim()

            if (fileName.isBlank()) continue
            if (!documentConversionService.isConvertible(fileName)) continue

            try {
                val downloadUrl = resolveBizInfoDownloadUrl(href, onclick)
                if (downloadUrl.isNullOrBlank()) continue

                val fileBytes = downloadBizInfoFile(downloadUrl)
                if (fileBytes == null || fileBytes.isEmpty()) {
                    continue
                }

                val originalGcsUrl =
                    gcsStorageService.uploadBytes(
                        bytes = fileBytes,
                        fileName = fileName,
                        directory = "$GCS_DIRECTORY/original",
                        contentType = getBizInfoContentType(fileName),
                    )
                originalUrls.add(originalGcsUrl)

                val pdfBytes = documentConversionService.convertToPdf(fileBytes, fileName)
                if (pdfBytes != null) {
                    val pdfFileName = fileName.substringBeforeLast(".") + ".pdf"
                    val pdfGcsUrl =
                        gcsStorageService.uploadBytes(
                            bytes = pdfBytes,
                            fileName = pdfFileName,
                            directory = "$GCS_DIRECTORY/pdf",
                            contentType = "application/pdf",
                        )
                    pdfUrls.add(pdfGcsUrl)
                } else if (fileName.lowercase().endsWith(".pdf")) {
                    pdfUrls.add(originalGcsUrl)
                }
            } catch (_: Exception) {
                continue
            }
        }

        return originalUrls to pdfUrls
    }

    private fun resolveBizInfoDownloadUrl(
        href: String,
        onclick: String,
    ): String? {
        return when {
            href.isNotBlank() && href != "#" -> {
                if (href.startsWith("http")) href else "$BIZINFO_BASE_URL$href"
            }
            onclick.isNotBlank() -> {
                val urlMatch = "['\"](https?://[^'\"]+)['\"]".toRegex().find(onclick)
                urlMatch?.groupValues?.get(1)
            }
            else -> null
        }
    }

    private fun downloadBizInfoFile(url: String): ByteArray? {
        return try {
            java.net.URI(url).toURL().openStream().use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }

    private fun getBizInfoContentType(fileName: String): String {
        return when (fileName.substringAfterLast(".").lowercase()) {
            "pdf" -> "application/pdf"
            "hwp" -> "application/x-hwp"
            "hwpx" -> "application/vnd.hancom.hwpx"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/octet-stream"
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
            when (announcement.source) {
                AnnouncementSource.K_STARTUP -> {
                    try {
                        var endDateString = announcement.receptionPeriod.split("~")[1].trim()
                        if (endDateString.contains(" ")) {
                            endDateString = endDateString.split(" ")[0]
                        }
                        val endDate = LocalDate.parse(endDateString, formatter)
                        if (endDate.isBefore(today)) {
                            announcement.status = AnnouncementStatus.INACTIVE
                            repository.save(announcement)
                        }
                    } catch (_: Exception) {
                        // 날짜 파싱 실패시 무시
                    }
                }
                AnnouncementSource.BIZINFO -> {
                    if (!isBizInfoAnnouncementExists(announcement.url)) {
                        announcement.status = AnnouncementStatus.INACTIVE
                        repository.save(announcement)
                    }
                }
            }
        }
    }

    private fun isBizInfoAnnouncementExists(url: String): Boolean {
        return try {
            val doc = Jsoup.connect(url).timeout(TIMEOUT_MS).get()
            val title = doc.selectFirst("h2.tit, .view_tit h2, .board_view h2, .tit_area h2, h2")?.text()?.trim()
            !title.isNullOrBlank()
        } catch (_: Exception) {
            false
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
        var isNaturalLanguageSearch = false
        var announcements =
            repository.searchAnnouncements(
                title = title,
                supportField = supportField,
                targetRegion = targetRegion,
                targetGroup = targetGroup,
                targetAge = targetAge,
                businessExperience = businessExperience,
                pageable = pageable,
            )

        if (announcements.isEmpty) {
            val queryBuilder = StringBuilder()
            title?.let { queryBuilder.append("제목: $it. ") }
            supportField?.let { queryBuilder.append("지원분야: $it. ") }
            targetRegion?.let { queryBuilder.append("지역: $it. ") }
            targetGroup?.let { queryBuilder.append("대상그룹: $it. ") }
            targetAge?.let { queryBuilder.append("대상연령: $it. ") }
            businessExperience?.let { queryBuilder.append("창업경험: $it. ") }

            val naturalLanguageRequest = NaturalLanguageSearchRequest(queryBuilder.toString().trim())
            val naturalLanguageResponseMono = searchAnnouncement(naturalLanguageRequest)
            val naturalLanguageResponse = naturalLanguageResponseMono.block()

            naturalLanguageResponse?.let {
                val titles = it.results.map { result -> result.title }
                val foundAnnouncements = repository.findAllByTitleIn(titles)

                val sortedFoundAnnouncements =
                    titles.mapNotNull { searchTitle ->
                        foundAnnouncements.find { announcement -> announcement.title == searchTitle }
                    }

                announcements = PageImpl(sortedFoundAnnouncements, pageable, sortedFoundAnnouncements.size.toLong())
                isNaturalLanguageSearch = true
            }
        }

        return if (includeLikeStatus) {
            mapAnnouncementsToResponseWithLikeStatus(announcements, isNaturalLanguageSearch)
        } else {
            announcements.map {
                AnnouncementResponse.from(it, isNatural = isNaturalLanguageSearch)
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
        val bmcInfos =
            userBmcs.map {
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
                .bodyToMono<RecommendationResponse>()
                .block()

        if (recommendationResponse == null || recommendationResponse.recommendations.isEmpty()) {
            return emptyList()
        }

        val scoreMap = recommendationResponse.recommendations.associate { it.title to it.score }
        val recommendedTitles = recommendationResponse.recommendations.map { it.title }

        val announcements = repository.findAllByTitleIn(recommendedTitles)

        val announcementsByTitle = announcements.associateBy { it.title }
        val sortedAnnouncements = recommendedTitles.mapNotNull { recommendedTitle -> announcementsByTitle[recommendedTitle] }

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

    @Transactional
    fun addLike(announcementId: Long) {
        val user = userAuthenticationHolder.current()
        val announcement =
            repository.findByIdOrNull(announcementId) ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        if (announcementLikeRepository.existsByUserAndAnnouncement(user, announcement)) {
            throw LikeAlreadyExistsException("이미 좋아요를 누른 공고")
        }

        val like =
            AnnouncementLike(
                user = user,
                announcement = announcement,
            )

        announcementLikeRepository.save(like)

        announcement.likeCount++
        repository.save(announcement)
    }

    @Transactional
    fun removeLike(announcementId: Long) {
        val user = userAuthenticationHolder.current()
        val announcement =
            repository.findByIdOrNull(announcementId) ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        val like =
            announcementLikeRepository.findByUserAndAnnouncement(user, announcement)
                ?: throw LikeNotFoundException("좋아요를 누르지 않은 공고")

        announcementLikeRepository.delete(like)

        announcement.likeCount--
        repository.save(announcement)
    }

    fun searchAnnouncement(request: NaturalLanguageSearchRequest): Mono<NaturalLanguageSearchResponse> {
        return webClient.post()
            .uri(searchServerUrl)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<NaturalLanguageSearchResponse>()
    }

    private fun mapAnnouncementsToResponseWithLikeStatus(
        announcements: Page<Announcement>,
        isNatural: Boolean? = null,
    ): Page<AnnouncementResponse> {
        val user = userAuthenticationHolder.current()
        val announcementList = announcements.content
        val userLikes = announcementLikeRepository.findAllByUserAndAnnouncementIn(user, announcementList)
        val likedAnnouncementIds = userLikes.map { it.announcement.id }.toSet()

        return announcements.map { announcement ->
            AnnouncementResponse.from(
                announcement = announcement,
                isLiked = likedAnnouncementIds.contains(announcement.id),
                isNatural = isNatural,
            )
        }
    }
}
