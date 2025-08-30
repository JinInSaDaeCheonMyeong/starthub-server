package com.jininsadaecheonmyeong.starthubserver.domain.announcement.docs

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Tag(name = "공고", description = "공고 관련 API")
interface AnnouncementDocs {
    @Operation(
        summary = "전체 공고 조회",
        description = "모집중인 공고를 전체 조회합니다. 인증된 사용자의 경우, 각 공고별 '좋아요' 여부를 함께 반환합니다.",
    )
    @Parameter(
        name = "userId",
        description = "(자동인식) 인증된 사용자의 ID",
        hidden = true,
    )
    @GetMapping
    fun getAllAnnouncements(
        @AuthenticationPrincipal userId: Long?,
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<Page<AnnouncementResponse>>>

    @Operation(
        summary = "공고 좋아요 추가",
        description = "특정 공고에 '좋아요'를 추가합니다.",
        security = [SecurityRequirement(name = "Authorization")],
    )
    @Parameter(name = "announcementId", description = "좋아요를 추가할 공고의 ID", `in` = ParameterIn.PATH)
    @PostMapping("/{announcementId}/likes")
    fun addLike(
        @AuthenticationPrincipal userId: Long,
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "공고 좋아요 삭제",
        description = "특정 공고의 '좋아요'를 삭제합니다.",
        security = [SecurityRequirement(name = "Authorization")],
    )
    @Parameter(name = "announcementId", description = "좋아요를 삭제할 공고의 ID", `in` = ParameterIn.PATH)
    @DeleteMapping("/{announcementId}/likes")
    fun removeLike(
        @AuthenticationPrincipal userId: Long,
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>>
}
