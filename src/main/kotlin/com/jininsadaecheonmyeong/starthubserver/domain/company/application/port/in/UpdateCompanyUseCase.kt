package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company

/**
 * 회사 정보 수정 UseCase (In Port)
 */
interface UpdateCompanyUseCase {
    fun updateCompany(command: UpdateCompanyCommand): Company
}

/**
 * 회사 정보 수정 Command
 */
data class UpdateCompanyCommand(
    val companyId: Long,
    val companyDescription: String?,
    val businessDescription: String?,
    val pageUrl: String?,
    val email: String?,
    val tel: String?,
    val address: String?,
    val employeeCount: Int?,
    val logoImage: String?
)
