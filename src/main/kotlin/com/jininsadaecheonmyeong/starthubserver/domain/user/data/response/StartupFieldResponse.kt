package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType

data class StartupFieldResponse(
    val businessType: BusinessType,
    val customField: String? = null,
)