package com.jininsadaecheonmyeong.starthubserver.exception.company

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class CompanyNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
