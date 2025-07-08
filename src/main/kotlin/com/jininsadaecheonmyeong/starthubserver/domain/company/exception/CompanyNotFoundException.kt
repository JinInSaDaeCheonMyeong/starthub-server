package com.jininsadaecheonmyeong.starthubserver.domain.company.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class CompanyNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
