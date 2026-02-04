package com.jininsadaecheonmyeong.starthubserver.presentation.controller.recruit

import com.jininsadaecheonmyeong.starthubserver.application.usecase.recruit.RecruitUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.CustomPageResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.recruit.RecruitDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.recruit.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.recruit.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.recruit.RecruitPreviewResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recruits")
class RecruitController(
    private val recruitUseCase: RecruitUseCase,
) : RecruitDocs {
    @PostMapping
    override fun createRecruit(
        @RequestBody request: CreateRecruitRequest,
    ) = BaseResponse.of(recruitUseCase.createRecruit(request), "채용 공고 생성 성공")

    @PatchMapping("/{id}")
    override fun updateRecruit(
        @PathVariable id: Long,
        @RequestBody request: UpdateRecruitRequest,
    ) = BaseResponse.of(recruitUseCase.updateRecruit(id, request), "채용 공고 수정 성공")

    @DeleteMapping("/{id}")
    override fun deleteRecruit(
        @PathVariable id: Long,
    ) = BaseResponse.of(recruitUseCase.deleteRecruit(id), "채용 공고 삭제 성공")

    @PatchMapping("/{id}/close")
    override fun closeRecruit(
        @PathVariable id: Long,
    ) = BaseResponse.of(recruitUseCase.closeRecruit(id), "채용 공고 마감 성공")

    @GetMapping
    override fun getAllRecruits(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<BaseResponse<CustomPageResponse<RecruitPreviewResponse>>> {
        val recruitsPage = recruitUseCase.getAllRecruits(page, size)
        val response = CustomPageResponse.from(recruitsPage)
        return BaseResponse.of(response, "채용 공고 목록 조회 성공")
    }

    @GetMapping("/{id}")
    override fun getRecruit(
        @PathVariable id: Long,
    ) = BaseResponse.of(recruitUseCase.getRecruit(id), "채용 공고 상세 조회 성공")
}
