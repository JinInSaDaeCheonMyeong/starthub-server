package com.jininsadaecheonmyeong.starthubserver.domain.user.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
import java.time.LocalDate

data class StartupFieldRequest(
    val businessType: BusinessType,
    val customField: String? = null,
)

data class UpdateUserProfileRequest(
    val username: String?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val birth: LocalDate?,
    val gender: UserGender?,
    val startupFields: List<StartupFieldRequest>?,
    val startupStatus: StartupStatus?,
    val companyName: String?,
    val companyDescription: String?,
    val numberOfEmployees: Int?,
    val companyWebsite: String?,
    val startupLocation: String?,
    val annualRevenue: Long?,
    val startupHistory: Int?,
)
