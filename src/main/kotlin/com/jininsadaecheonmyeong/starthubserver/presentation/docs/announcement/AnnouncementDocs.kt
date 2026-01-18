package com.jininsadaecheonmyeong.starthubserver.presentation.docs.announcement

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.CustomPageResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementDetailResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.AnnouncementResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.announcement.RecommendedAnnouncementResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "공고", description = "공고 관련 API")
interface AnnouncementDocs {
    @Operation(
        summary = "전체 공고 조회",
        description = "모집중인 공고를 전체 조회합니다. `includeLikeStatus` 파라미터를 true로 주면, 인증된 사용자의 경우 각 공고별 '좋아요' 여부를 함께 반환합니다.",
    )
    @GetMapping
    fun getAllAnnouncements(
        @ParameterObject pageable: Pageable,
        @Parameter(description = "'좋아요' 여부 포함 여부") @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>>

    @Operation(
        summary = "좋아요 누른 공고 조회",
        description = "현재 로그인한 사용자가 좋아요를 누른 공고 목록을 최신순으로 조회합니다.",
    )
    @GetMapping("/likes")
    fun getLikedAnnouncements(
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>>

    @Operation(
        summary = "공고 좋아요 추가",
        description = "특정 공고에 '좋아요'를 추가합니다.",
    )
    @PostMapping("/{announcementId}/likes")
    fun addLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "공고 좋아요 삭제",
        description = "특정 공고의 '좋아요'를 삭제합니다.",
    )
    @DeleteMapping("/{announcementId}/likes")
    fun removeLike(
        @PathVariable announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "공고 상세 조회",
        description = "특정 공고의 상세 정보를 조회합니다. `includeLikeStatus` 파라미터를 true로 주면, 인증된 사용자의 경우 해당 공고의 '좋아요' 여부를 함께 반환합니다.",
    )
    @GetMapping("/{announcementId}")
    fun getAnnouncementDetail(
        @PathVariable announcementId: Long,
        @Parameter(description = "'좋아요' 여부 포함 여부") @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
    ): ResponseEntity<BaseResponse<AnnouncementDetailResponse>>

    @Operation(
        summary = "공고 검색",
        description = "공고의 제목과 필터로 검색합니다. 여러 필터를 동시에 사용할 수 있습니다. `includeLikeStatus` 파라미터를 true로 주면, 인증된 사용자의 경우 각 공고별 '좋아요' 여부를 함께 반환합니다.",
    )
    @GetMapping("/search")
    fun searchAnnouncements(
        @Parameter(description = "제목으로 검색") @RequestParam(required = false) title: String?,
        @Parameter(description = "지원분야 필터") @RequestParam(required = false) supportField: String?,
        @Parameter(description = "지역 필터") @RequestParam(required = false) targetRegion: String?,
        @Parameter(description = "대상 필터") @RequestParam(required = false) targetGroup: String?,
        @Parameter(description = "연령 필터") @RequestParam(required = false) targetAge: String?,
        @Parameter(description = "창업업력 필터") @RequestParam(required = false) businessExperience: String?,
        @Parameter(description = "'좋아요' 여부 포함 여부") @RequestParam(defaultValue = "false") includeLikeStatus: Boolean,
        @ParameterObject pageable: Pageable,
    ): ResponseEntity<BaseResponse<CustomPageResponse<AnnouncementResponse>>>

    @Operation(
        summary = "추천 공고 조회",
        description = "사용자의 창업 분야를 기반으로 추천된 공고 목록을 조회합니다.",
    )
    @GetMapping("/recommendations")
    fun getRecommendedAnnouncements(): ResponseEntity<BaseResponse<List<RecommendedAnnouncementResponse>>>
}
