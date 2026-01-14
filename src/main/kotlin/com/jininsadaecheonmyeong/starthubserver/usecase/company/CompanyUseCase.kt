package com.jininsadaecheonmyeong.starthubserver.usecase.company

import com.jininsadaecheonmyeong.starthubserver.dto.request.company.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.company.UpdateCompanyProfileRequest
import com.jininsadaecheonmyeong.starthubserver.entity.company.Company
import com.jininsadaecheonmyeong.starthubserver.enums.user.BusinessType

interface CompanyUseCase {
    fun save(req: CreateCompanyRequest)

    fun delete(id: Long)

    fun update(
        id: Long,
        req: UpdateCompanyProfileRequest,
    )

    fun findAll(): List<Company>

    fun findById(id: Long): Company?

    fun findByCompanyName(name: String): Company?

    fun findByCategory(category: BusinessType): List<Company>

    fun findMy(): List<Company>
}
