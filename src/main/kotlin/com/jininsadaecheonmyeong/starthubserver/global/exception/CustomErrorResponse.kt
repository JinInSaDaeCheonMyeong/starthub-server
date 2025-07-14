package com.jininsadaecheonmyeong.starthubserver.global.exception

import java.time.LocalDateTime

data class CustomErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
