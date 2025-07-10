package com.jininsadaecheonmyeong.starthubserver.domain.company.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.UpdateCompanyProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyDuplicationException
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.NotCompanyFounderException
import com.jininsadaecheonmyeong.starthubserver.domain.company.repository.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CompanyService (
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
    fun delete(id: UUID) {
        val user = UserAuthenticationHolder.current()
        val company = repository.findById(id).orElseThrow { CompanyNotFoundException("찾을 수 없는 기업") }
        throwExceptionWhenUserIsNotFounder(user, company)
        company.delete()
        repository.save(company)
    }

    @Transactional
    fun update(id: UUID, req: UpdateCompanyProfileRequest) {
        val user = UserAuthenticationHolder.current()
        val company = repository.findById(id).orElseThrow { CompanyNotFoundException("찾을 수 없는 기업") }
        throwExceptionWhenUserIsNotFounder(user, company)
        company.updateProfile(req)
        repository.save(company)
    }

    private fun throwExceptionWhenUserIsNotFounder(user: User, company: Company) {
        if (!company.isFounder(user)) throw NotCompanyFounderException("기업 등록자만 접근할 수 있습니다.")
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Company> {
        return repository.findAllByDeletedFalse()
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Company? {
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