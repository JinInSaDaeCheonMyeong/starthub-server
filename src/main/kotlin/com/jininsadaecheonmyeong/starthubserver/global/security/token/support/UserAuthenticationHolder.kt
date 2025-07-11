package com.jininsadaecheonmyeong.starthubserver.global.security.token.support

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import org.springframework.security.core.context.SecurityContextHolder

object UserAuthenticationHolder {
    fun current(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is CustomUserDetails) {
            throw InvalidTokenException("인증되지 않은 사용자입니다.")
        }
        return principal.user
    }
}
