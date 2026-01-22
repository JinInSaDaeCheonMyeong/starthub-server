package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.company

import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType
import java.time.LocalDateTime

data class CompanyResponse(
    val id: Long,
    val companyName: String,
    val companyDescription: String,
    val companyCategory: BusinessType,
    val businessDescription: String,
    val founderId: Long,
    val founderName: String?,
    val companyUrl: String?,
    val contactEmail: String,
    val contactNumber: String,
    val address: String?,
    val employeeCount: Int,
    val logoImage: String?,
    val createdAt: LocalDateTime?,
)
