package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.`in`

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

/**
 * 회사 조회 UseCase (In Port)
 * - 다양한 조건으로 회사를 조회
 */
interface GetCompanyUseCase {
    fun getById(id: Long): Company?
    fun getByName(name: String): Company?
    fun getByCategory(category: BusinessType): List<Company>
    fun getAll(): List<Company>
}
