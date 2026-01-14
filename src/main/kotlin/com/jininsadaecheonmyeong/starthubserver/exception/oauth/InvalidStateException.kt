package com.jininsadaecheonmyeong.starthubserver.exception.oauth

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidStateException(message: String) : CustomException(message, HttpStatus.BAD_REQUEST)
