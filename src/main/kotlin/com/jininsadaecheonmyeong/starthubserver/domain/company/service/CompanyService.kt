package com.jininsadaecheonmyeong.starthubserver.domain.company.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.UpdateCompanyProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyDuplicationException
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.NotCompanyFounderException
import com.jininsadaecheonmyeong.starthubserver.domain.company.repository.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyService(
    private val repository: CompanyRepository,
) {
    @Transactional
    fun save(req: CreateCompanyRequest) {
        val founder = UserAuthenticationHolder.current()
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
        val user = UserAuthenticationHolder.current()
        val company = repository.findById(companyId).orElseThrow { CompanyNotFoundException("찾을 수 없는 기업") }
        if (!company.isFounder(user)) {
            throw NotCompanyFounderException("기업 등록자만 접근할 수 있습니다.")
        }
        return company
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Company> {
        return repository.findAllByDeletedFalse()
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Company? {
        return repository.findByIdAndDeletedFalse(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findByCompanyName(name: String): Company? {
        return repository.findByCompanyNameAndDeletedFalse(name).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findByCategory(category: BusinessType): List<Company> {
        return repository.findByCompanyCategoryAndDeletedFalse(category)
    }

    @Transactional(readOnly = true)
    fun findMy(): List<Company> {
        val user = UserAuthenticationHolder.current()
        return repository.findByFounderAndDeletedFalse(user)
    }
}
