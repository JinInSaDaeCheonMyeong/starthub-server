package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.authentication

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.GetCurrentUserPort
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextUserAdapter(
    private val userAuthenticationHolder: UserAuthenticationHolder
) : GetCurrentUserPort {
    override fun getCurrentUserId(): Long {
        return userAuthenticationHolder.current().id
            ?: throw IllegalStateException("User ID not found in security context")
    }
}
