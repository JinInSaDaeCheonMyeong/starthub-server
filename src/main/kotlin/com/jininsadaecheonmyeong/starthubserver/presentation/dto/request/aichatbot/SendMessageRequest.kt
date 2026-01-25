package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot

import jakarta.validation.constraints.NotBlank

data class SendMessageRequest(
    @field:NotBlank(message = "메시지를 입력해주세요.")
    val message: String,
)
