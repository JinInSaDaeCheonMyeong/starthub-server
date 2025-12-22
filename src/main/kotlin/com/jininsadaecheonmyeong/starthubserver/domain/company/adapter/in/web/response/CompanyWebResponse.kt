package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.`in`.web.response

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import java.time.LocalDateTime

/**
 * 회사 상세 정보 Web Response
 * - Domain Model을 Web 응답으로 변환
 */
data class CompanyWebResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val businessDescription: String,
    val founderId: Long,
    val companyUrl: String?,
    val contactEmail: String,
    val contactNumber: String,
    val address: String?,
    val employeeCount: Int,
    val logoImage: String?,
    val createdAt: LocalDateTime?
) {
    companion object {
        /**
         * Domain Model -> Web Response 변환
         */
        fun from(company: Company) = CompanyWebResponse(
            id = company.id!!,
            companyName = company.companyName,
            companyDescription = company.profile.companyDescription,
            companyCategory = company.profile.companyCategory,
            businessDescription = company.profile.businessDescription,
            founderId = company.founderId,
            companyUrl = company.profile.companyUrl,
            contactEmail = company.profile.contactEmail,
            contactNumber = company.profile.contactNumber,
            address = company.profile.address,
            employeeCount = company.profile.employeeCount,
            logoImage = company.profile.logoImage,
            createdAt = company.createdAt
        )
    }
}
