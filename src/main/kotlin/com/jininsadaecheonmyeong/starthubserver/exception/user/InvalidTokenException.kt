package com.jininsadaecheonmyeong.starthubserver.exception.user

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidTokenException(message: String) : CustomException(message, HttpStatus.UNAUTHORIZED)
