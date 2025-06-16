package com.jininsadaecheonmyeong.starthubserver.domain.company.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

data class CompanyListResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val logoImage: String? = null,
) {
    companion object {
        fun from(company: Company): CompanyListResponse {
            return CompanyListResponse(
                id = company.id!!,
                companyName = company.companyName,
                companyDescription = company.companyDescription,
                companyCategory = company.companyCategory,
                logoImage = company.logoImage,
            )
        }

        fun fromList(companies: List<Company>): List<CompanyListResponse> {
            return companies.map { from(it) }
        }
    }
}