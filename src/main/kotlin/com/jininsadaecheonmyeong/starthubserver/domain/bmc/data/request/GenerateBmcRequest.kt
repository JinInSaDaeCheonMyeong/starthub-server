package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GenerateBmcRequest(
    @field:NotBlank(message = "세션 ID를 입력해주세요.")
    val sessionId: String,

    @field:NotBlank(message = "BMC 제목을 입력해주세요.")
    @field:Size(min = 1, max = 100, message = "BMC 제목은 1자 이상 100자 이하로 입력해주세요.")
    val title: String
)