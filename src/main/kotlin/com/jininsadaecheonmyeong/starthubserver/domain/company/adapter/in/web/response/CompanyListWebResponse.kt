package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

/**
 * 회사 목록 Web Response
 * - 목록 조회 시 사용하는 간소화된 응답
 */
data class CompanyListWebResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val logoImage: String?
) {
    companion object {
        /**
         * Domain Model -> Web Response 변환
         */
        fun from(company: Company) = CompanyListWebResponse(
            id = company.id!!,
            companyName = company.companyName,
            companyDescription = company.profile.companyDescription,
            companyCategory = company.profile.companyCategory,
            logoImage = company.profile.logoImage
        )
    }
}
