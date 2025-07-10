package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

data class ModifyBmcRequest(
    @field:NotNull(message = "BMC ID를 입력해주세요.")
    val bmcId: UUID,

    @field:NotBlank(message = "수정 요청 내용을 입력해주세요.")
    @field:Size(min = 10, max = 1000, message = "수정 요청은 10자 이상 1000자 이하로 입력해주세요.")
    val modificationRequest: String,

    @field:NotNull(message = "요청 타입을 선택해주세요.")
    val requestType: BmcModificationType = BmcModificationType.MODIFY
)