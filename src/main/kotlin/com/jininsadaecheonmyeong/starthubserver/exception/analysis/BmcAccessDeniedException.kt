package com.jininsadaecheonmyeong.starthubserver.exception.analysis

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BmcAccessDeniedException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)
