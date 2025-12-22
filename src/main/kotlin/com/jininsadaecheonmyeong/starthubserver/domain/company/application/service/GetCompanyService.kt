package com.jininsadaecheonmyeong.starthubserver.domain.company.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.GetCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회사 조회 Service
 * - GetCompanyUseCase 구현
 * - 다양한 조건으로 회사 조회
 */
@Service
@Transactional(readOnly = true)
class GetCompanyService(
    private val loadCompanyPort: LoadCompanyPort
) : GetCompanyUseCase {

    override fun getById(id: Long): Company? {
        return loadCompanyPort.loadById(id)
    }

    override fun getByName(name: String): Company? {
        return loadCompanyPort.loadByName(name)
    }

    override fun getByCategory(category: BusinessType): List<Company> {
        return loadCompanyPort.loadByCategory(category)
    }

    override fun getAll(): List<Company> {
        return loadCompanyPort.loadAll()
    }
}
