package com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class QuotaExceededException(
    message: String,
    val resetAt: LocalDateTime? = null,
) : CustomException(message, HttpStatus.TOO_MANY_REQUESTS)
