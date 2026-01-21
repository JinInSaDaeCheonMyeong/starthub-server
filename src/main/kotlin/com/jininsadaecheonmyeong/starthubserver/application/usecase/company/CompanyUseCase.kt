package com.jininsadaecheonmyeong.starthubserver.application.usecase.company

import com.jininsadaecheonmyeong.starthubserver.domain.entity.company.Company
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.exception.company.CompanyDuplicationException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.company.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.company.NotCompanyFounderException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.company.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.company.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.company.UpdateCompanyProfileRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class CompanyUseCase(
    private val repository: CompanyRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) {
    @Transactional
    fun save(req: CreateCompanyRequest) {
        val founder = userAuthenticationHolder.current()
        if (repository.existsByCompanyNameAndDeletedFalse(req.name)) {
            throw CompanyDuplicationException("이미 등록된 기업")
        }
        repository.save(req.toEntity(founder))
    }

    @Transactional
    fun delete(id: Long) {
        val company = findCompanyAndVerifyFounder(id)
        company.delete()
        repository.save(company)
    }

    @Transactional
    fun update(
        id: Long,
        req: UpdateCompanyProfileRequest,
    ) {
        val company = findCompanyAndVerifyFounder(id)
        company.updateProfile(req)
        repository.save(company)
    }

    private fun findCompanyAndVerifyFounder(companyId: Long): Company {
        val user = userAuthenticationHolder.current()
        val company = repository.findByIdOrNull(companyId) ?: throw CompanyNotFoundException("찾을 수 없는 기업")
        if (!company.isFounder(user)) {
            throw NotCompanyFounderException("기업 등록자만 접근할 수 있습니다.")
        }
        return company
    }

    fun findAll(): List<Company> {
        return repository.findAllByDeletedFalse()
    }

    fun findById(id: Long): Company? {
        return repository.findByIdAndDeletedFalse(id)
    }

    fun findByCompanyName(name: String): Company? {
        return repository.findByCompanyNameAndDeletedFalse(name)
    }

    fun findByCategory(category: BusinessType): List<Company> {
        return repository.findByCompanyCategoryAndDeletedFalse(category)
    }

    fun findMy(): List<Company> {
        val user = userAuthenticationHolder.current()
        return repository.findByFounderAndDeletedFalse(user)
    }
}
