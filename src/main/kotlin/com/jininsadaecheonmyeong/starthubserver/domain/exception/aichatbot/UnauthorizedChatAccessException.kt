package com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class UnauthorizedChatAccessException(
    message: String,
) : CustomException(message, HttpStatus.FORBIDDEN)
