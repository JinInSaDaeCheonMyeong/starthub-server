package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc

import jakarta.validation.constraints.NotNull

data class GenerateBmcRequest(
    @field:NotNull(message = "세션 ID를 입력해주세요.")
    var sessionId: Long,
)
