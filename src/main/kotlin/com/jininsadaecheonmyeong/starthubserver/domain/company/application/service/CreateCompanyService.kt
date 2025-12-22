package com.jininsadaecheonmyeong.starthubserver.domain.company.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.CreateCompanyCommand
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`.CreateCompanyUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.LoadCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.SaveCompanyPort
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception.CompanyDuplicationException
import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회사 생성 Service
 * - CreateCompanyUseCase 구현
 * - Port를 통한 의존성 주입
 * - 비즈니스 로직은 Domain Model에 위임
 */
@Service
@Transactional
class CreateCompanyService(
    private val loadCompanyPort: LoadCompanyPort,
    private val saveCompanyPort: SaveCompanyPort,
    private val getCurrentUserPort: GetCurrentUserPort
) : CreateCompanyUseCase {

    override fun createCompany(command: CreateCompanyCommand): Company {
        // 중복 검증
        if (loadCompanyPort.existsByName(command.name)) {
            throw CompanyDuplicationException("이미 등록된 기업: ${command.name}")
        }

        // 현재 사용자 조회
        val founderId = getCurrentUserPort.getCurrentUserId()

        // Domain Model 생성 (Factory Method 사용)
        val company = Company.create(
            companyName = command.name,
            companyDescription = command.companyDescription,
            category = command.category,
            businessDescription = command.businessDescription,
            founderId = founderId,
            companyUrl = command.pageUrl,
            contactEmail = command.email,
            contactNumber = command.tel,
            address = command.address,
            employeeCount = command.employeeCount,
            logoImage = command.logoImage
        )

        // 저장 및 반환
        return saveCompanyPort.save(company)
    }
}
