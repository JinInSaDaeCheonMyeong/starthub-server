package com.jininsadaecheonmyeong.starthubserver.domain.exception.email

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class ExpiredEmailException(message: String) : CustomException(message, HttpStatus.CONFLICT)
