package com.jininsadaecheonmyeong.starthubserver.domain.exception.bmc

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BmcSessionNotCompletedException(message: String) : CustomException(message, HttpStatus.BAD_REQUEST)
