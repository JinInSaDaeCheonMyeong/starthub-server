package com.jininsadaecheonmyeong.starthubserver.domain.recruit.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitPreviewResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.docs.RecruitDocs
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.service.RecruitService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recruits")
class RecruitController(
    private val recruitService: RecruitService,
) : RecruitDocs {
    @PostMapping
    override fun createRecruit(
        request: CreateRecruitRequest,
    ): ResponseEntity<BaseResponse<RecruitResponse>> {
        return BaseResponse.of(recruitService.createRecruit(request), "채용 공고 생성 성공")
    }

    @PatchMapping("/{id}")
    override fun updateRecruit(
        @PathVariable id: Long,
        request: UpdateRecruitRequest,
    ): ResponseEntity<BaseResponse<RecruitResponse>> {
        return BaseResponse.of(recruitService.updateRecruit(id, request), "채용 공고 수정 성공")
    }

    @DeleteMapping("/{id}")
    override fun deleteRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        recruitService.deleteRecruit(id)
        return BaseResponse.of("채용 공고 삭제 성공")
    }

    @PatchMapping("/{id}/close")
    override fun closeRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        recruitService.closeRecruit(id)
        return BaseResponse.of("채용 공고 마감 성공")
    }

    @GetMapping
    override fun getAllRecruits(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<BaseResponse<Page<RecruitPreviewResponse>>> {
        return BaseResponse.of(recruitService.getAllRecruits(page, size), "채용 공고 목록 조회 성공")
    }

    @GetMapping("/{id}")
    override fun getRecruit(
        @PathVariable id: Long,
    ): ResponseEntity<BaseResponse<RecruitResponse>> {
        return BaseResponse.of(recruitService.getRecruit(id), "채용 공고 상세 조회 성공")
    }
}
