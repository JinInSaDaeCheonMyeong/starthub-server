package com.jininsadaecheonmyeong.starthubserver.domain.exception.user

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class UserNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
