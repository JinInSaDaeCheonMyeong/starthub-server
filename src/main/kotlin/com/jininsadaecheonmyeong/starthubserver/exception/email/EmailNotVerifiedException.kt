package com.jininsadaecheonmyeong.starthubserver.exception.email

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class EmailNotVerifiedException(message: String) : CustomException(message, HttpStatus.UNAUTHORIZED)
