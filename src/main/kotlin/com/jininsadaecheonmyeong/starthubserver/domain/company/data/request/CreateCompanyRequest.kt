package com.jininsadaecheonmyeong.starthubserver.domain.company.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.support.Phone
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min

data class CreateCompanyRequest(
    val name: String,
    val companyDescription: String,
    val category: BusinessType,
    val businessDescription: String,
    val pageUrl: String? = null,
    @field:Email
    val email: String,
    @field:Phone
    val tel: String,
    val address: String? = null,
    @field:Min(value = 1, message = "사원 수 누락됨")
    val employeeCount: Int,
    val logoImage: String? = null,
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
            address = address,
            employeeCount = employeeCount,
            logoImage = logoImage,
        )
}
