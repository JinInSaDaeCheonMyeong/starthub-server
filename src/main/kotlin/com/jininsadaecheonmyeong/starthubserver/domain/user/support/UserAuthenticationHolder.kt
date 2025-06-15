package com.jininsadaecheonmyeong.starthubserver.domain.user.support

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.CustomUserDetails
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
final class UserAuthenticationHolder {
    fun current(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return (principal as CustomUserDetails).user
    }

    companion object {
        fun getUserAgent(request: HttpServletRequest): String? {
            return request.getHeader("User-Agent")
        }
    }
}