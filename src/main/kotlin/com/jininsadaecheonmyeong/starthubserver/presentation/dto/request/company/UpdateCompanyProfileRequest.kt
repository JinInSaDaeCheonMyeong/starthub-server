package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.company

import com.jininsadaecheonmyeong.starthubserver.global.support.Phone
import jakarta.validation.constraints.Email

data class UpdateCompanyProfileRequest(
    val companyDescription: String? = null,
    val businessDescription: String? = null,
    val pageUrl: String? = null,
    @field:Email
    val email: String? = null,
    @field:Phone
    val tel: String? = null,
    val address: String? = null,
    val employeeCount: Int? = null,
    val logoImage: String? = null,
)
