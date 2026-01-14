package com.jininsadaecheonmyeong.starthubserver.controller.announcement

import com.jininsadaecheonmyeong.starthubserver.docs.announcement.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.dto.response.announcement.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.announcement.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.announcement.RecommendedAnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.CustomPageResponse
import com.jininsadaecheonmyeong.starthubserver.usecase.announcement.AnnouncementUseCase
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementUseCase: AnnouncementUseCase,
) : AnnouncementDocs {
    override fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
        @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements = announcementUseCase.findAllAnnouncements(pageable, includeLikeStatus)
        val response = CustomPageResponse.from(announcements)
        return BaseResponse.of(response, "공고 조회 성공")
    }

    override fun getLikedAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements = announcementUseCase.findLikedAnnouncementsByUser(pageable)
        val response = CustomPageResponse.from(announcements)
        return BaseResponse.of(response, "좋아요 누른 공고 조회 성공")
    }

    override fun addLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementUseCase.addLike(announcementId)
        return BaseResponse.of(Unit, "좋아요 추가 성공")
    }

    override fun removeLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementUseCase.removeLike(announcementId)
        return BaseResponse.of(Unit, "좋아요 삭제 성공")
    }

    override fun getAnnouncementDetail(
        @PathVariable announcementId: Long,
        @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<AnnouncementDetailResponse>> {
        val response = announcementUseCase.getAnnouncementDetail(announcementId, includeLikeStatus)
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
            announcementUseCase.searchAnnouncements(
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

    override fun getRecommendedAnnouncements(): ResponseEntity<BaseResponse<List<RecommendedAnnouncementResponse>>> {
        val response = announcementUseCase.getRecommendedAnnouncements()
        return BaseResponse.of(response, "추천 공고 조회 성공")
    }
}
