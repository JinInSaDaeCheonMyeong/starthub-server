package com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BusinessModelCanvasNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)