package com.jininsadaecheonmyeong.starthubserver.dto.request.bmc

import com.jininsadaecheonmyeong.starthubserver.enums.bmc.BmcTemplateType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateBmcSessionRequest(
    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하로 입력해주세요.")
    val title: String,
    val templateType: BmcTemplateType,
)
