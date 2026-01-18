package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user

import com.fasterxml.jackson.annotation.JsonFormat
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.UserGender
import java.time.LocalDate

data class UserProfileResponse(
    val username: String?,
    val profileImage: String?,
    val companyIds: List<Long>,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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
)
