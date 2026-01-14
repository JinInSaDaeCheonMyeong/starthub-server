package com.jininsadaecheonmyeong.starthubserver.dto.response.user

import com.jininsadaecheonmyeong.starthubserver.enums.user.BusinessType

data class StartupFieldResponse(
    val businessType: BusinessType,
    val customField: String? = null,
)
