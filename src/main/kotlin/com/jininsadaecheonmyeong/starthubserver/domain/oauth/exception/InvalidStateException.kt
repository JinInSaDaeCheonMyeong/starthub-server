package com.jininsadaecheonmyeong.starthubserver.domain.oauth.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidStateException(message: String) : CustomException(message, HttpStatus.BAD_REQUEST)