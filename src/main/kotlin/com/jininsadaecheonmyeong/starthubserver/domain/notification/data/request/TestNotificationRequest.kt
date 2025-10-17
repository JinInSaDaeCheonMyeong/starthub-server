package com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request

import jakarta.validation.constraints.NotBlank

data class TestNotificationRequest(
    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,
    @field:NotBlank(message = "내용은 필수입니다.")
    val body: String,
    val data: Map<String, String> = emptyMap(),
)
