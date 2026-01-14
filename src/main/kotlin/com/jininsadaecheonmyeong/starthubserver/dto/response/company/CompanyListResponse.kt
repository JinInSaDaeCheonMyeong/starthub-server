package com.jininsadaecheonmyeong.starthubserver.dto.response.company

import com.jininsadaecheonmyeong.starthubserver.enums.user.BusinessType

data class CompanyListResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val logoImage: String? = null,
)
