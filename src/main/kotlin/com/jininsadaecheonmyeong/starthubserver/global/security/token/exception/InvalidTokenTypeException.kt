package com.jininsadaecheonmyeong.starthubserver.global.security.token.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidTokenTypeException(message: String = "잘못된 토큰 형식") : CustomException(message, HttpStatus.UNAUTHORIZED)
