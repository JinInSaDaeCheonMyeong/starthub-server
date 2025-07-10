package com.jininsadaecheonmyeong.starthubserver.domain.company.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import java.time.LocalDateTime
import java.util.UUID

data class CompanyResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val businessDescription: String,
    val founderId: UUID,
    val founderName: String?,
    val companyUrl: String?,
    val contactEmail: String,
    val contactNumber: String,
    val address: String?,
    val employeeCount: Int,
    val logoImage: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)
