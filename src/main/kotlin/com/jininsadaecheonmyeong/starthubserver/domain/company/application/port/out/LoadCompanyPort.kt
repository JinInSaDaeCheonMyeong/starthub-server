package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.company.domain.model.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

/**
 * 회사 조회 Out Port
 * - Persistence Layer에서 구현
 * - 의존성 역전 원칙 (DIP): Application이 인터페이스를 정의, Adapter가 구현
 */
interface LoadCompanyPort {
    fun loadById(id: Long): Company?
    fun loadByName(name: String): Company?
    fun loadByCategory(category: BusinessType): List<Company>
    fun loadByFounderId(founderId: Long): List<Company>
    fun loadAll(): List<Company>
    fun existsByName(name: String): Boolean
}
