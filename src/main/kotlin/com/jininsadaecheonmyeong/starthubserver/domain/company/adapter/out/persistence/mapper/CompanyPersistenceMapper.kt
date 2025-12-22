package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.mapper

import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.entity.CompanyJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.value.CompanyProfile
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.stereotype.Component

/**
 * Company Persistence Mapper
 * - Domain Model ↔ JPA Entity 변환
 * - 계층 간 데이터 변환의 핵심
 */
@Component
class CompanyPersistenceMapper {

    /**
     * Domain Model -> JPA Entity
     */
    fun toJpaEntity(domain: Company, founder: User): CompanyJpaEntity {
        return CompanyJpaEntity(
            id = domain.id,
            companyName = domain.companyName,
            companyDescription = domain.profile.companyDescription,
            companyCategory = domain.profile.companyCategory,
            businessDescription = domain.profile.businessDescription,
            founder = founder,
            companyUrl = domain.profile.companyUrl,
            contactEmail = domain.profile.contactEmail,
            contactNumber = domain.profile.contactNumber,
            address = domain.profile.address,
            employeeCount = domain.profile.employeeCount,
            logoImage = domain.profile.logoImage,
            deleted = domain.deleted
        ).apply {
            // BaseEntity 필드 설정
            domain.createdAt?.let { this.createdAt = it }
            domain.updatedAt?.let { this.updatedAt = it }
        }
    }

    /**
     * JPA Entity -> Domain Model
     */
    fun toDomain(entity: CompanyJpaEntity): Company {
        val profile = CompanyProfile(
            companyDescription = entity.companyDescription,
            companyCategory = entity.companyCategory,
            businessDescription = entity.businessDescription,
            companyUrl = entity.companyUrl,
            contactEmail = entity.contactEmail,
            contactNumber = entity.contactNumber,
            address = entity.address,
            employeeCount = entity.employeeCount,
            logoImage = entity.logoImage
        )

        return Company(
            id = entity.id,
            companyName = entity.companyName,
            profile = profile,
            founderId = entity.founder.id!!,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deleted = entity.deleted
        )
    }
}
