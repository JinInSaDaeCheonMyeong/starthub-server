package com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 회사를 찾을 수 없을 때 발생하는 예외
 */
class CompanyNotFoundException(message: String = "찾을 수 없는 기업") :
    CustomException(message, HttpStatus.NOT_FOUND)
