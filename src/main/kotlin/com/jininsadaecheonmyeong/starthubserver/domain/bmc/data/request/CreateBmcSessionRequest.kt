package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateBmcSessionRequest(
    @field:NotBlank(message = "사업 아이디어를 입력해주세요.")
    @field:Size(min = 10, max = 500, message = "사업 아이디어는 10자 이상 500자 이하로 입력해주세요.")
    val businessIdea: String
)