package com.jininsadaecheonmyeong.starthubserver.domain.company.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.DeleteCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.SaveCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.NotCompanyFounderException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회사 삭제 Service
 * - DeleteCompanyUseCase 구현
 * - Soft Delete 처리
 */
@Service
@Transactional
class DeleteCompanyService(
    private val loadCompanyPort: LoadCompanyPort,
    private val saveCompanyPort: SaveCompanyPort,
    private val getCurrentUserPort: GetCurrentUserPort
) : DeleteCompanyUseCase {

    override fun deleteCompany(companyId: Long) {
        // 회사 조회
        val company = loadCompanyPort.loadById(companyId)
            ?: throw CompanyNotFoundException("찾을 수 없는 기업: $companyId")

        // 권한 검증
        val currentUserId = getCurrentUserPort.getCurrentUserId()
        if (!company.isFounder(currentUserId)) {
            throw NotCompanyFounderException()
        }

        // Soft Delete (Domain Model의 비즈니스 로직 사용)
        val deletedCompany = company.delete()
        saveCompanyPort.save(deletedCompany)
    }
}
