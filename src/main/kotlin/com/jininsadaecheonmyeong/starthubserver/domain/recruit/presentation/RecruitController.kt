
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.service.RecruitService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recruits")
class RecruitController(
    private val recruitService: RecruitService,
) {
    @PostMapping
    fun createRecruit(
        @RequestBody @Valid request: CreateRecruitRequest,
    ): ResponseEntity<BaseResponse<Any>> {
        val response = recruitService.createRecruit(request)
        return BaseResponse.of(response, "채용 공고 생성 성공")
    }

    @GetMapping("/{recruitId}")
    fun getRecruit(
        @PathVariable recruitId: Long,
    ): ResponseEntity<BaseResponse<Any>> {
        val response = recruitService.getRecruit(recruitId)
        return BaseResponse.of(response, "채용 공고 조회 성공")
    }

    @GetMapping
    fun getAllRecruits(): ResponseEntity<BaseResponse<Any>> {
        val response = recruitService.getAllRecruits()
        return BaseResponse.of(response, "모든 채용 공고 조회 성공")
    }

    @PatchMapping("/{recruitId}")
    fun updateRecruit(
        @PathVariable recruitId: Long,
        @RequestBody @Valid request: UpdateRecruitRequest,
    ): ResponseEntity<BaseResponse<Any>> {
        val response = recruitService.updateRecruit(recruitId, request)
        return BaseResponse.of(response, "채용 공고 수정 성공")
    }

    @DeleteMapping("/{recruitId}")
    fun deleteRecruit(
        @PathVariable recruitId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        return BaseResponse.of(recruitService.deleteRecruit(recruitId), "채용 공고 삭제 성공")
    }
}
