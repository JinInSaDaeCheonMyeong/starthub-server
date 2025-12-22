package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.`in`.web

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.`in`.web.response.AnnouncementDetailWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.`in`.web.response.AnnouncementWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.AddLikeUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetAnnouncementDetailUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetAnnouncementsUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.GetLikedAnnouncementsUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.`in`.RemoveLikeUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.RecommendedAnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val getAnnouncementsUseCase: GetAnnouncementsUseCase,
    private val getAnnouncementDetailUseCase: GetAnnouncementDetailUseCase,
    private val getLikedAnnouncementsUseCase: GetLikedAnnouncementsUseCase,
    private val addLikeUseCase: AddLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    // Temporary: delegate AI features to old services until they are converted
    private val announcementService: AnnouncementService
) : AnnouncementDocs {

    @GetMapping
    override fun getAllAnnouncements(
        pageable: Pageable,
        @RequestParam(required = false, defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<Page<AnnouncementWebResponse>>> {
        val results = getAnnouncementsUseCase.getAnnouncements(pageable, includeLikeStatus)
        val responses = results.map { result ->
            AnnouncementWebResponse.from(result.announcement, result.isLiked)
        }
        return BaseResponse.of(responses, HttpStatus.OK, "공고 목록 조회 성공")
    }

    @GetMapping("/liked")
    override fun getLikedAnnouncements(
        pageable: Pageable,
    ): ResponseEntity<BaseResponse<Page<AnnouncementWebResponse>>> {
        val results = getLikedAnnouncementsUseCase.getLikedAnnouncements(pageable)
        val responses = results.map { result ->
            AnnouncementWebResponse.from(result.announcement, result.isLiked)
        }
        return BaseResponse.of(responses, HttpStatus.OK, "좋아요한 공고 목록 조회 성공")
    }

    @PostMapping("/{announcementId}/like")
    override fun addLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        addLikeUseCase.addLike(announcementId)
        return BaseResponse.of(Unit, HttpStatus.OK, "좋아요 성공")
    }

    @DeleteMapping("/{announcementId}/like")
    override fun removeLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        removeLikeUseCase.removeLike(announcementId)
        return BaseResponse.of(Unit, HttpStatus.OK, "좋아요 취소 성공")
    }

    @GetMapping("/{announcementId}")
    override fun getAnnouncementDetail(
        @PathVariable announcementId: Long,
        @RequestParam(required = false, defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<AnnouncementDetailWebResponse>> {
        val result = getAnnouncementDetailUseCase.getAnnouncementDetail(announcementId, includeLikeStatus)
        val response = AnnouncementDetailWebResponse.from(result.announcement, result.isLiked)
        return BaseResponse.of(response, HttpStatus.OK, "공고 상세 조회 성공")
    }

    @GetMapping("/search")
    override fun searchAnnouncements(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) supportField: String?,
        @RequestParam(required = false) targetRegion: String?,
        @RequestParam(required = false) targetGroup: String?,
        @RequestParam(required = false) targetAge: String?,
        @RequestParam(required = false) businessExperience: String?,
        @RequestParam(required = false, defaultValue = "false") includeLikeStatus: Boolean,
        @RequestParam(required = false, defaultValue = "false") natural: Boolean,
        pageable: Pageable,
    ): ResponseEntity<BaseResponse<Any>> {
        // Temporary: delegate to old service until AI search is converted
        val results = announcementService.searchAnnouncements(
            title, supportField, targetRegion, targetGroup, targetAge, businessExperience,
            includeLikeStatus, pageable
        )
        return BaseResponse.of(results, HttpStatus.OK, "공고 검색 성공")
    }

    @GetMapping("/recommended")
    override fun getRecommendedAnnouncements(): ResponseEntity<BaseResponse<List<RecommendedAnnouncementResponse>>> {
        // Temporary: delegate to old service until AI recommendation is converted
        val recommendations = announcementService.getRecommendedAnnouncements()
        return BaseResponse.of(recommendations, HttpStatus.OK, "추천 공고 조회 성공")
    }
}
