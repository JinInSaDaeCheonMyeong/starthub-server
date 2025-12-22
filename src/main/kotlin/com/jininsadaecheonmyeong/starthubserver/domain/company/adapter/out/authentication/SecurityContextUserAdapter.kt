package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.authentication

import com.jininsadaecheonmyeong.starthubserver.domain.company.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Security Context User Adapter
 * - GetCurrentUserPort 구현
 * - Spring Security Context에서 현재 사용자 ID 조회
 */
@Component
class SecurityContextUserAdapter : GetCurrentUserPort {

    override fun getCurrentUserId(): Long {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is String) {
            throw InvalidTokenException("인증되지 않은 사용자입니다.")
        }
        return principal.toLong()
    }
}
