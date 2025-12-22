package com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 중복된 회사명으로 등록 시도 시 발생하는 예외
 */
class CompanyDuplicationException(message: String = "이미 등록된 기업") :
    CustomException(message, HttpStatus.CONFLICT)
