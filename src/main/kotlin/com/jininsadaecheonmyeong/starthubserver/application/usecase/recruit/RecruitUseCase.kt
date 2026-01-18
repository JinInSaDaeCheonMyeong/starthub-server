package com.jininsadaecheonmyeong.starthubserver.application.usecase.recruit

import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.recruit.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.recruit.UpdateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.recruit.RecruitPreviewResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.recruit.RecruitResponse
import org.springframework.data.domain.Page

interface RecruitUseCase {
    fun createRecruit(request: CreateRecruitRequest): RecruitResponse

    fun updateRecruit(
        recruitId: Long,
        request: UpdateRecruitRequest,
    ): RecruitResponse

    fun deleteRecruit(recruitId: Long)

    fun closeRecruit(recruitId: Long)

    fun getAllRecruits(
        page: Int,
        size: Int,
    ): Page<RecruitPreviewResponse>

    fun getRecruit(recruitId: Long): RecruitResponse
}
