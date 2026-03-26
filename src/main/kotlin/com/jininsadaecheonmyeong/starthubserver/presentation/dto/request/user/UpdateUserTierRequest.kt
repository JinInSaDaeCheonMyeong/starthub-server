package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user

import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.UserTier
import jakarta.validation.constraints.NotNull

data class UpdateUserTierRequest(
    @field:NotNull(message = "대상 유저 ID는 필수입니다.")
    val userId: Long,
    @field:NotNull(message = "등급은 필수입니다.")
    val tier: UserTier,
)
