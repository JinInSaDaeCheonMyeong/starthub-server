package com.jininsadaecheonmyeong.starthubserver.domain.company.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

data class CreateCompanyRequest(
    val name: String,
    val companyDescription: String,
    val category: BusinessType,
    val businessDescription: String,
    val pageUrl: String? = null,
    val email: String,
    val tel: String,
    val address: String? = null
) {
    fun toEntity(founder: User): Company =
        Company(
            companyName = name,
            companyDescription = companyDescription,
            companyCategory = category,
            businessDescription = businessDescription,
            founder = founder,
            companyUrl = pageUrl,
            contactEmail = email,
            contactNumber = tel,
            address = address
        )

}
