package com.jininsadaecheonmyeong.starthubserver.domain.analysis.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BmcAccessDeniedException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)
