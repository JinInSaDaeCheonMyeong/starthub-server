package com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.value.CompanyProfile
import java.time.LocalDateTime

/**
 * Company Domain Model
 * - JPA 의존성 제거 (순수 Kotlin)
 * - 불변성 원칙 (val 사용)
 * - 비즈니스 로직 캡슐화
 */
data class Company(
    val id: Long? = null,
    val companyName: String,
    val profile: CompanyProfile,
    val founderId: Long,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val deleted: Boolean = false
) {
    init {
        require(companyName.isNotBlank()) { "회사명은 필수입니다" }
        require(founderId > 0) { "유효하지 않은 창업자 ID입니다" }
    }

    /**
     * 창업자 여부 확인
     */
    fun isFounder(userId: Long): Boolean = founderId == userId

    /**
     * 회사 프로필 업데이트 (불변성 원칙 - 새로운 객체 반환)
     */
    fun updateProfile(
        companyDescription: String? = null,
        businessDescription: String? = null,
        pageUrl: String? = null,
        email: String? = null,
        tel: String? = null,
        address: String? = null,
        employeeCount: Int? = null,
        logoImage: String? = null
    ): Company {
        val updatedProfile = profile.copy(
            companyDescription = companyDescription ?: profile.companyDescription,
            businessDescription = businessDescription ?: profile.businessDescription,
            companyUrl = pageUrl ?: profile.companyUrl,
            contactEmail = email ?: profile.contactEmail,
            contactNumber = tel ?: profile.contactNumber,
            address = address ?: profile.address,
            employeeCount = employeeCount ?: profile.employeeCount,
            logoImage = logoImage ?: profile.logoImage
        )
        return copy(profile = updatedProfile, updatedAt = LocalDateTime.now())
    }

    /**
     * 회사 삭제 (Soft Delete, 불변성 원칙)
     */
    fun delete(): Company = copy(deleted = true)

    /**
     * 삭제 여부 확인
     */
    fun isDeleted(): Boolean = deleted

    companion object {
        /**
         * 새로운 회사 생성 (Factory Method)
         */
        fun create(
            companyName: String,
            companyDescription: String,
            category: com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType,
            businessDescription: String,
            founderId: Long,
            companyUrl: String?,
            contactEmail: String,
            contactNumber: String,
            address: String?,
            employeeCount: Int,
            logoImage: String?
        ): Company {
            val profile = CompanyProfile(
                companyDescription = companyDescription,
                companyCategory = category,
                businessDescription = businessDescription,
                companyUrl = companyUrl,
                contactEmail = contactEmail,
                contactNumber = contactNumber,
                address = address,
                employeeCount = employeeCount,
                logoImage = logoImage
            )
            return Company(
                companyName = companyName,
                profile = profile,
                founderId = founderId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}
