package com.jininsadaecheonmyeong.starthubserver.domain.exception.user

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class EmailAlreadyExistsException(message: String) : CustomException(message, HttpStatus.CONFLICT)
