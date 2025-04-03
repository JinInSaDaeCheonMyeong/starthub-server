package com.jininsadaecheonmyeong.starthubserver.global.security.token.support

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.security.core.context.SecurityContextHolder

object UserAuthenticationHolder {
    fun current(): User {
        return (SecurityContextHolder.getContext().authentication.principal as CustomUserDetails).user
    }
}