package com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

@Tag(name = "공고", description = "공고 조회 API")
interface AnnouncementDocs {
    @Operation(summary = "공고 조회", description = "모집중인 공고를 전체 조회합니다.")
    @GetMapping
    fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<Page<AnnouncementResponse>>>
}
