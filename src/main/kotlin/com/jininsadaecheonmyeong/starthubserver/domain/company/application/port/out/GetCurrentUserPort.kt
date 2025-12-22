package com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out

/**
 * 현재 인증된 사용자 ID를 조회하는 Out Port
 * - Security Context에서 구현
 */
interface GetCurrentUserPort {
    fun getCurrentUserId(): Long
}
