package com.jininsadaecheonmyeong.starthubserver.domain.company.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.GetMyCompaniesUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 내 회사 목록 조회 Service
 * - GetMyCompaniesUseCase 구현
 */
@Service
@Transactional(readOnly = true)
class GetMyCompaniesService(
    private val loadCompanyPort: LoadCompanyPort,
    private val getCurrentUserPort: GetCurrentUserPort
) : GetMyCompaniesUseCase {

    override fun getMyCompanies(): List<Company> {
        val currentUserId = getCurrentUserPort.getCurrentUserId()
        return loadCompanyPort.loadByFounderId(currentUserId)
    }
}
