package com.jininsadaecheonmyeong.starthubserver.global.security.token.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class ExpiredTokenException(message: String = "토큰 만료됨") : CustomException(message, HttpStatus.UNAUTHORIZED)