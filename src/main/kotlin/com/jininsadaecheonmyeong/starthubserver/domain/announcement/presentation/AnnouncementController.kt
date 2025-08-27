package com.jininsadaecheonmyeong.starthubserver.domain.announcement.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs.AnnouncementDocs
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.service.AnnouncementService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
) : AnnouncementDocs {
    override fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<Page<AnnouncementResponse>>> {
        val announcements = announcementService.findAllAnnouncements(pageable)
        return BaseResponse.of(announcements, "공고 조회 성공")
    }
}
