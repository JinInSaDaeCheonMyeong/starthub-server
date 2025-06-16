package com.jininsadaecheonmyeong.starthubserver.domain.company.data.request

import jakarta.validation.constraints.Email

data class UpdateCompanyProfileRequest(
    val companyDescription: String? = null,
    val businessDescription: String? = null,
    val pageUrl: String? = null,
    @field:Email
    val email: String? = null,
    val tel: String? = null,
    val address: String? = null,
    val employeeCount: Int? = null,
    val logoImage: String? = null
)
