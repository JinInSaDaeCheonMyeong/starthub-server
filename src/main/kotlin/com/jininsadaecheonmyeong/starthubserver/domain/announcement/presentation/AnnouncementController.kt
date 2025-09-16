package com.jininsadaecheonmyeong.starthubserver.domain.announcement.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.RecommendedAnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementLikeService
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.CustomPageResponse
import jakarta.servlet.http.HttpServletRequest
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
    private val announcementLikeService: AnnouncementLikeService,
) : AnnouncementDocs {
    override fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
        @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements = announcementService.findAllAnnouncements(pageable, includeLikeStatus)
        val response = CustomPageResponse.from(announcements)
        return BaseResponse.of(response, "공고 조회 성공")
    }

    override fun getLikedAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements = announcementService.findLikedAnnouncementsByUser(pageable)
        val response = CustomPageResponse.from(announcements)
        return BaseResponse.of(response, "좋아요 누른 공고 조회 성공")
    }

    override fun addLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementLikeService.addLike(announcementId)
        return BaseResponse.of(Unit, "좋아요 추가 성공")
    }

    override fun removeLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementLikeService.removeLike(announcementId)
        return BaseResponse.of(Unit, "좋아요 삭제 성공")
    }

    override fun getAnnouncementDetail(announcementId: Long): ResponseEntity<BaseResponse<AnnouncementDetailResponse>> {
        val response = announcementService.getAnnouncementDetail(announcementId)
        return BaseResponse.of(response, "공고 상세 조회 성공")
    }

    override fun searchAnnouncements(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) supportField: String?,
        @RequestParam(required = false) targetRegion: String?,
        @RequestParam(required = false) targetGroup: String?,
        @RequestParam(required = false) targetAge: String?,
        @RequestParam(required = false) businessExperience: String?,
        @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements =
            announcementService.searchAnnouncements(
                title = title,
                supportField = supportField,
                targetRegion = targetRegion,
                targetGroup = targetGroup,
                targetAge = targetAge,
                businessExperience = businessExperience,
                includeLikeStatus = includeLikeStatus,
                pageable = pageable,
            )
        val response = CustomPageResponse.from(announcements)
        return BaseResponse.of(response, "공고 검색 성공")
    }

    override fun getRecommendedAnnouncements(request: HttpServletRequest): ResponseEntity<BaseResponse<List<RecommendedAnnouncementResponse>>> {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val accessToken = authHeader.substring(7)
        val response = announcementService.getRecommendedAnnouncements(accessToken)
        return BaseResponse.of(response, "추천 공고 조회 성공")
    }
}
