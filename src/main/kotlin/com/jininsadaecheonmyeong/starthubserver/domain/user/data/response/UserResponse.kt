package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
import java.time.LocalDate

data class UserResponse(
    val id: Long,
    val email: String,
    val username: String?,
    val birth: LocalDate?,
    val gender: UserGender?,
    val startupStatus: StartupStatus?,
    val companyName: String?,
    val companyDescription: String?,
    val numberOfEmployees: Int?,
    val companyWebsite: String?,
    val startupLocation: String?,
    val annualRevenue: Long?,
    val startupFields: List<BusinessType>,
    val startupHistory: Int?,
    val provider: AuthType,
)
