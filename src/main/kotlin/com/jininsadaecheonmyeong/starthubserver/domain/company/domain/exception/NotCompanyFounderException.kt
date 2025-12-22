package com.jininsadaecheonmyeong.starthubserver.domain.company.domain.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 회사 창업자가 아닌 사용자가 권한이 필요한 작업을 시도할 때 발생하는 예외
 */
class NotCompanyFounderException(message: String = "기업 등록자만 접근할 수 있습니다.") :
    CustomException(message, HttpStatus.FORBIDDEN)
