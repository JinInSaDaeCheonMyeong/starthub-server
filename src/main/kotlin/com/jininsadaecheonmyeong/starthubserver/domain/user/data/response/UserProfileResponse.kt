package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
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
    val startupFields: List<BusinessType>,
)
