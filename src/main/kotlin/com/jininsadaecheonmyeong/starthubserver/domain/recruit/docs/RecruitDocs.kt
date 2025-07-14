package com.jininsadaecheonmyeong.starthubserver.domain.recruit.docs

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitPreviewResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Recruit", description = "채용 공고 관련 API")
interface RecruitDocs {
    @Operation(
        summary = "채용 공고 생성",
        description = "새로운 채용 공고를 생성합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채용 공고 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
        ],
    )
    fun createRecruit(
        @Valid @RequestBody request: CreateRecruitRequest,
    ): ResponseEntity<BaseResponse<RecruitResponse>>

    @Operation(
        summary = "채용 공고 수정",
        description = "기존 채용 공고를 수정합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채용 공고 수정 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음",
            ),
            ApiResponse(
                responseCode = "404",
                description = "채용 공고를 찾을 수 없음",
            ),
        ],
    )
    fun updateRecruit(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRecruitRequest,
    ): ResponseEntity<BaseResponse<RecruitResponse>>

    @Operation(
        summary = "채용 공고 삭제",
        description = "채용 공고를 삭제합니다. Soft Delete로 처리됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채용 공고 삭제 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음",
            ),
            ApiResponse(
                responseCode = "404",
                description = "채용 공고를 찾을 수 없음",
            ),
        ],
    )
    fun deleteRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "채용 공고 마감",
        description = "채용 공고를 마감합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채용 공고 마감 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음",
            ),
            ApiResponse(
                responseCode = "404",
                description = "채용 공고를 찾을 수 없음",
            ),
        ],
    )
    fun closeRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "채용 공고 목록 조회",
        description = "모든 채용 공고 목록을 페이지로 조회합니다.",
    )
    fun getAllRecruits(
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): ResponseEntity<BaseResponse<Page<RecruitPreviewResponse>>>

    @Operation(
        summary = "채용 공고 상세 조회",
        description = "특정 채용 공고의 상세 정보를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채용 공고 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "채용 공고를 찾을 수 없음",
            ),
        ],
    )
    fun getRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<RecruitResponse>>
}
