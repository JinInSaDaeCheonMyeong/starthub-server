package com.jininsadaecheonmyeong.starthubserver.dto.response.user

import com.jininsadaecheonmyeong.starthubserver.enums.user.AuthType
import com.jininsadaecheonmyeong.starthubserver.enums.user.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.enums.user.UserGender
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
    val startupFields: List<StartupFieldResponse>,
    val startupHistory: Int?,
    val provider: AuthType,
)
