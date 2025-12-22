package com.jininsadaecheonmyeong.starthubserver.domain.company.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.UpdateCompanyCommand
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.UpdateCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.SaveCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.NotCompanyFounderException
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회사 정보 수정 Service
 * - UpdateCompanyUseCase 구현
 */
@Service
@Transactional
class UpdateCompanyService(
    private val loadCompanyPort: LoadCompanyPort,
    private val saveCompanyPort: SaveCompanyPort,
    private val getCurrentUserPort: GetCurrentUserPort
) : UpdateCompanyUseCase {

    override fun updateCompany(command: UpdateCompanyCommand): Company {
        // 회사 조회
        val company = loadCompanyPort.loadById(command.companyId)
            ?: throw CompanyNotFoundException("찾을 수 없는 기업: ${command.companyId}")

        // 권한 검증
        val currentUserId = getCurrentUserPort.getCurrentUserId()
        if (!company.isFounder(currentUserId)) {
            throw NotCompanyFounderException()
        }

        // 프로필 업데이트 (Domain Model의 비즈니스 로직 사용)
        val updatedCompany = company.updateProfile(
            companyDescription = command.companyDescription,
            businessDescription = command.businessDescription,
            pageUrl = command.pageUrl,
            email = command.email,
            tel = command.tel,
            address = command.address,
            employeeCount = command.employeeCount,
            logoImage = command.logoImage
        )

        // 저장 및 반환
        return saveCompanyPort.save(updatedCompany)
    }
}
