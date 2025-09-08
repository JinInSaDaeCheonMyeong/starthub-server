package com.jininsadaecheonmyeong.starthubserver.domain.announcement.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementLikeService
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.CustomPageResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
    private val announcementLikeService: AnnouncementLikeService,
) : AnnouncementDocs {
    override fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>> {
        val announcements = announcementService.findAllAnnouncements(pageable)
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
}
