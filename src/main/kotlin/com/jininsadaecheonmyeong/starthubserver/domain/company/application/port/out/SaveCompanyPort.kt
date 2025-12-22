package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company

/**
 * 회사 저장 Out Port
 * - Persistence Layer에서 구현
 */
interface SaveCompanyPort {
    fun save(company: Company): Company
}
