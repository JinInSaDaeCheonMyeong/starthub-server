package com.jininsadaecheonmyeong.starthubserver.domain.company.data.request

data class UpdateCompanyProfileRequest(
    val companyDescription: String? = null,
    val businessDescription: String? = null,
    val pageUrl: String? = null,
    val email: String? = null,
    val tel: String? = null,
    val address: String? = null
)
