package com.jininsadaecheonmyeong.starthubserver.domain.email.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class EmailNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
