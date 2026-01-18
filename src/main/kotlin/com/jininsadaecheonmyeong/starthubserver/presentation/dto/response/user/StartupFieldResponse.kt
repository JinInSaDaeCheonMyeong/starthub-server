package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user

import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType

data class StartupFieldResponse(
    val businessType: BusinessType,
    val customField: String? = null,
)
