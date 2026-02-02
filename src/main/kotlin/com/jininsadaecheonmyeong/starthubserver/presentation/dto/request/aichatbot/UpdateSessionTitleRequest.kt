package com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.aichatbot

import jakarta.validation.constraints.NotBlank

data class UpdateSessionTitleRequest(
    @field:NotBlank(message = "제목을 입력해주세요.")
    val title: String,
)
