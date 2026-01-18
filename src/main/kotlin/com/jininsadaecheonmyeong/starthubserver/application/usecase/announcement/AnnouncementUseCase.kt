package com.jininsadaecheonmyeong.starthubserver.application.usecase.announcement

import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.announcement.NaturalLanguageSearchRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.NaturalLanguageSearchResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.RecommendedAnnouncementResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface AnnouncementUseCase {
    fun scrapeAndSaveAnnouncements()

    fun findAllAnnouncements(
        pageable: Pageable,
        includeLikeStatus: Boolean,
    ): Page<AnnouncementResponse>

    fun deactivateExpiredAnnouncements()

    fun findLikedAnnouncementsByUser(pageable: Pageable): Page<AnnouncementResponse>

    fun getAnnouncementDetail(
        announcementId: Long,
        includeLikeStatus: Boolean,
    ): AnnouncementDetailResponse

    fun searchAnnouncements(
        title: String?,
        supportField: String?,
        targetRegion: String?,
        targetGroup: String?,
        targetAge: String?,
        businessExperience: String?,
        includeLikeStatus: Boolean,
        pageable: Pageable,
    ): Page<AnnouncementResponse>

    fun getRecommendedAnnouncements(): List<RecommendedAnnouncementResponse>

    fun addLike(announcementId: Long)

    fun removeLike(announcementId: Long)

    fun searchAnnouncement(request: NaturalLanguageSearchRequest): Mono<NaturalLanguageSearchResponse>
}
