package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.properties.TokenProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component

@Component
class TokenParser(
    private val properties: TokenProperties,
) {
    fun findType(token: String): TokenType {
        return TokenType.toTokenType(createClaims(token)["category"].toString())
    }

    fun findEmail(token: String): String {
        return createClaims(token)["email"].toString()
    }

    private fun createClaims(token: String): Claims {
        return Jwts.parser().verifyWith(properties.secretKey).build().parseSignedClaims(token).payload
    }
}
