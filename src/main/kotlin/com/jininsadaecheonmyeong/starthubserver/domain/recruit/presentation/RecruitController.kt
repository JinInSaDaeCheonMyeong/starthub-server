package com.jininsadaecheonmyeong.starthubserver.domain.recruit.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.service.RecruitService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/recruit")
class RecruitController(
    private val recruitService: RecruitService
) {
    @PostMapping("/{companyId}")
    fun createRecruit(
        @PathVariable companyId: UUID,
        @RequestBody request: CreateRecruitRequest
    ): ResponseEntity<BaseResponse<RecruitResponse>> =
        BaseResponse.of(recruitService.create(companyId, request), "모집 포지션 등록 성공")

    @GetMapping("/{companyId}")
    fun getRecruitsByCompany(
        @PathVariable companyId: UUID
    ): ResponseEntity<BaseResponse<List<RecruitResponse>>> =
        BaseResponse.of(recruitService.getByCompany(companyId), "모집 포지션 조회 성공")
}