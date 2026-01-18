package com.jininsadaecheonmyeong.starthubserver.application.usecase.company

import com.jininsadaecheonmyeong.starthubserver.domain.entity.company.Company
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.company.CreateCompanyRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.company.UpdateCompanyProfileRequest

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
