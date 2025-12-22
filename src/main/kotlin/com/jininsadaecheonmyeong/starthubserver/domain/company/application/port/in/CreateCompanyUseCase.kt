package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

/**
 * 회사 생성 UseCase (In Port)
 * - "무엇을 할 수 있는가"를 정의하는 인터페이스
 */
interface CreateCompanyUseCase {
    fun createCompany(command: CreateCompanyCommand): Company
}

/**
 * 회사 생성 Command
 * - Web Request와 Domain을 분리하기 위한 Command 객체
 */
data class CreateCompanyCommand(
    val name: String,
    val companyDescription: String,
    val category: BusinessType,
    val businessDescription: String,
    val pageUrl: String?,
    val email: String,
    val tel: String,
    val address: String?,
    val employeeCount: Int,
    val logoImage: String?
)
