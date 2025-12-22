package com.jininsadaecheonmyeong.starthubserver.domain.company.domain.value

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

/**
 * Company Profile Value Object
 * - 회사 프로필 관련 정보를 그룹화
 * - 불변성 보장 (data class)
 * - 비즈니스 규칙 검증
 */
data class CompanyProfile(
    val companyDescription: String,
    val companyCategory: BusinessType,
    val businessDescription: String,
    val companyUrl: String?,
    val contactEmail: String,
    val contactNumber: String,
    val address: String?,
    val employeeCount: Int,
    val logoImage: String?
) {
    init {
        require(companyDescription.isNotBlank()) { "회사 설명은 필수입니다" }
        require(businessDescription.isNotBlank()) { "사업 설명은 필수입니다" }
        require(contactEmail.isNotBlank()) { "연락 이메일은 필수입니다" }
        require(contactNumber.isNotBlank()) { "연락 번호는 필수입니다" }
        require(employeeCount > 0) { "직원 수는 1 이상이어야 합니다" }
    }
}
