package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import jakarta.validation.constraints.NotNull

data class GenerateBmcRequest(
    @field:NotNull(message = "세션 ID를 입력해주세요.")
    val sessionId: Long,
)
