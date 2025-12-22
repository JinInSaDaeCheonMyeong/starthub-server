package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company

/**
 * 내가 등록한 회사 목록 조회 UseCase (In Port)
 */
interface GetMyCompaniesUseCase {
    fun getMyCompanies(): List<Company>
}
