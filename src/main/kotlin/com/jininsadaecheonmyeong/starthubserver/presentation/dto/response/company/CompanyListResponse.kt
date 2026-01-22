package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.company

import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType

data class CompanyListResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val logoImage: String? = null,
)
