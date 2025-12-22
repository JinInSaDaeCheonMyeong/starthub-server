package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence

import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.mapper.CompanyPersistenceMapper
import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.repository.CompanyJpaRepository
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadFounderPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.SaveCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import org.springframework.stereotype.Component

/**
 * Company Persistence Adapter
 * - Out Port 구현 (LoadCompanyPort, SaveCompanyPort)
 * - Domain과 Infrastructure의 경계
 */
@Component
class CompanyPersistenceAdapter(
    private val jpaRepository: CompanyJpaRepository,
    private val mapper: CompanyPersistenceMapper,
    private val loadFounderPort: LoadFounderPort
) : LoadCompanyPort, SaveCompanyPort {

    override fun loadById(id: Long): Company? {
        return jpaRepository.findByIdAndDeletedFalse(id)
            ?.let { mapper.toDomain(it) }
    }

    override fun loadByName(name: String): Company? {
        return jpaRepository.findByCompanyNameAndDeletedFalse(name)
            ?.let { mapper.toDomain(it) }
    }

    override fun loadByCategory(category: BusinessType): List<Company> {
        return jpaRepository.findByCompanyCategoryAndDeletedFalse(category)
            .map { mapper.toDomain(it) }
    }

    override fun loadByFounderId(founderId: Long): List<Company> {
        val founder = loadFounderPort.loadById(founderId)
            ?: return emptyList()
        return jpaRepository.findByFounderAndDeletedFalse(founder)
            .map { mapper.toDomain(it) }
    }

    override fun loadAll(): List<Company> {
        return jpaRepository.findAllByDeletedFalse()
            .map { mapper.toDomain(it) }
    }

    override fun existsByName(name: String): Boolean {
        return jpaRepository.existsByCompanyNameAndDeletedFalse(name)
    }

    override fun save(company: Company): Company {
        val founder = loadFounderPort.loadById(company.founderId)
            ?: throw IllegalArgumentException("Founder not found: ${company.founderId}")

        val jpaEntity = if (company.id != null) {
            // Update existing
            jpaRepository.findById(company.id).orElseThrow()
                .apply {
                    companyName = company.companyName
                    companyDescription = company.profile.companyDescription
                    companyCategory = company.profile.companyCategory
                    businessDescription = company.profile.businessDescription
                    companyUrl = company.profile.companyUrl
                    contactEmail = company.profile.contactEmail
                    contactNumber = company.profile.contactNumber
                    address = company.profile.address
                    employeeCount = company.profile.employeeCount
                    logoImage = company.profile.logoImage
                    deleted = company.deleted
                }
        } else {
            // Create new
            mapper.toJpaEntity(company, founder)
        }

        val saved = jpaRepository.save(jpaEntity)
        return mapper.toDomain(saved)
    }
}
