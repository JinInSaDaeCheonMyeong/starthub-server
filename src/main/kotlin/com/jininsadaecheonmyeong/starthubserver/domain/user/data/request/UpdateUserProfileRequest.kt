package com.jininsadaecheonmyeong.starthubserver.domain.user.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
import java.time.LocalDate

data class UpdateUserProfileRequest(
    val username: String,
    val introduction: String,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val birth: LocalDate,
    val gender: UserGender,
    val interests: List<BusinessType>,
    val profileImage: String,
)
