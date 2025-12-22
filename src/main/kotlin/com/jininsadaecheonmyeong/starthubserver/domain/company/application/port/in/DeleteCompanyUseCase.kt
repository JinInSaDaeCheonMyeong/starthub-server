package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`

/**
 * 회사 삭제 UseCase (In Port)
 */
interface DeleteCompanyUseCase {
    fun deleteCompany(companyId: Long)
}
