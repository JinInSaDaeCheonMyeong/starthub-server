package com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BmcSessionNotCompletedException(message: String) : CustomException(message, HttpStatus.BAD_REQUEST)