package com.jininsadaecheonmyeong.starthubserver.domain.announcement.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementLikeService
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
    private val announcementLikeService: AnnouncementLikeService,
) : AnnouncementDocs {
    override fun getAllAnnouncements(
        @AuthenticationPrincipal userId: Long?,
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<Page<AnnouncementResponse>>> {
        val announcements = announcementService.findAllAnnouncements(userId, pageable)
        return BaseResponse.of(announcements, "공고 조회 성공")
    }

    override fun addLike(
        @AuthenticationPrincipal userId: Long,
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementLikeService.addLike(userId, announcementId)
        return BaseResponse.of(Unit, "좋아요 추가 성공")
    }

    override fun removeLike(
        @AuthenticationPrincipal userId: Long,
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        announcementLikeService.removeLike(userId, announcementId)
        return BaseResponse.of(Unit, "좋아요 삭제 성공")
    }
}
