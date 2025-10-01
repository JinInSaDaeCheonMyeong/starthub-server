package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.properties.TokenProperties
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.lang.System.currentTimeMillis
import java.util.Date

@Component
class TokenProvider(
    private val properties: TokenProperties,
    private val tokenRedisService: TokenRedisService,
) {
    private fun generate(
        tokenType: TokenType,
        user: User,
        expire: Long,
    ): String {
        return Jwts.builder()
            .claim("category", tokenType.value)
            .subject(user.id.toString())
            .issuedAt(Date(currentTimeMillis()))
            .expiration(Date(currentTimeMillis() + expire))
            .signWith(properties.secretKey)
            .compact()
    }

    fun generateAccess(user: User) = generate(TokenType.ACCESS_TOKEN, user, properties.access)

    fun generateRefresh(user: User): String {
        val refreshToken = generate(TokenType.REFRESH_TOKEN, user, properties.refresh)
        tokenRedisService.storeRefreshToken(user.id.toString(), refreshToken)
        return refreshToken
    }
}
