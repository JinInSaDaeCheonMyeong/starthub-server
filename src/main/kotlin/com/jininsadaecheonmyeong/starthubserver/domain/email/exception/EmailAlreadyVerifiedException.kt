package com.jininsadaecheonmyeong.starthubserver.domain.email.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class EmailAlreadyVerifiedException(message: String) : CustomException(message, HttpStatus.CONFLICT)
