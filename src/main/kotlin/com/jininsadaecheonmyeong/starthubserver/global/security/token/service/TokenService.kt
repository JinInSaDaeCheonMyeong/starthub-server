package com.jininsadaecheonmyeong.starthubserver.global.security.token.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenRedisService
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val tokenProvider: TokenProvider,
    private val tokenRedisService: TokenRedisService,
) {
    fun generateAndStoreRefreshToken(user: User): String {
        val refreshToken = tokenProvider.generateRefresh(user)
        tokenRedisService.storeRefreshToken(user.id.toString(), refreshToken)
        return refreshToken
    }

    fun generateAccess(user: User) = tokenProvider.generateAccess(user)
}
