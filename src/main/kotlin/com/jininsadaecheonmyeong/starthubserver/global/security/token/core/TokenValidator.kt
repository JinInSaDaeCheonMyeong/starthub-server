package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component

@Component
class TokenValidator(
    private val parser: TokenParser,
) {
    fun validateAll(
        token: String,
        tokenType: TokenType,
    ) {
        validate(token)
        validateType(token, tokenType)
    }

    fun validateType(
        token: String,
        tokenType: TokenType,
    ) {
        if (parser.findType(token) != tokenType) throw InvalidTokenException("토큰이 일치하지 않음")
    }

    fun validate(token: String) {
        try {
            parser.findType(token)
        } catch (_: ExpiredJwtException) {
            throw ExpiredTokenException("토큰 만료됨")
        } catch (_: SignatureException) {
            throw InvalidTokenException("유효하지 않은 토큰 시그니처")
        } catch (_: Exception) {
            throw InvalidTokenException("알 수 없는 토큰")
        } catch (_: MalformedJwtException) {
            throw MalformedJwtException("잘못된 토큰 형식")
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("토큰이 유효하지 않거나 비어있음")
        }
    }
}
