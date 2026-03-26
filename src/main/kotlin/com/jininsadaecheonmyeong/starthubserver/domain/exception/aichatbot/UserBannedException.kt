package com.jininsadaecheonmyeong.starthubserver.domain.exception.aichatbot

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class UserBannedException(
    message: String = "계정이 차단되었습니다. 관리자에게 문의해주세요.",
) : CustomException(message, HttpStatus.FORBIDDEN)
