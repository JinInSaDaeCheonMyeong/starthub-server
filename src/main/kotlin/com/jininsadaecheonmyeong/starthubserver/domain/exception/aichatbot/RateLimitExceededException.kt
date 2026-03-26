package com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class RateLimitExceededException(
    message: String = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
) : CustomException(message, HttpStatus.TOO_MANY_REQUESTS)
