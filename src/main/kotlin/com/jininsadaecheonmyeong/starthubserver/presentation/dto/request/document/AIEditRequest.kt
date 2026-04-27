package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.document

import jakarta.validation.constraints.NotBlank

data class AIEditRequest(
    @field:NotBlank(message = "수정 요청 내용을 입력해주세요.")
    val prompt: String,
)
