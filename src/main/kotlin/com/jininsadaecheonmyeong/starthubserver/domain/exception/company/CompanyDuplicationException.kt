package com.jininsadaecheonmyeong.starthubserver.domain.exception.company

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class CompanyDuplicationException(message: String) : CustomException(message, HttpStatus.CONFLICT)
