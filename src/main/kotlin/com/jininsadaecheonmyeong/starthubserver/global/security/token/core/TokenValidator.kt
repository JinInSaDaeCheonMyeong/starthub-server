package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.ExpiredTokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenValidator(
    private val parser: TokenParser,
    private val tokenRedisService: TokenRedisService
) {
    fun validateAll(token: String, tokenType: TokenType) {
        validate(token)
        validateType(token, tokenType)
        validateNotBlocked(token)
    }

    fun validateType(token: String, tokenType: TokenType) {
        if (parser.findType(token) != tokenType) throw InvalidTokenException("토큰이 일치하지 않음")
    }

    fun validateNotBlocked(token: String) {
        if (tokenRedisService.isTokenBlocked(token)) throw ExpiredTokenException("토큰이 무효화되었습니다")
    }

    fun validate(token: String) {
        try {
            parser.findType(token)
            if (parser.findExpiration(token).before(Date()))
                throw ExpiredTokenException("토큰 만료됨")
        } catch (e: ExpiredJwtException) {
            throw ExpiredTokenException("토큰 만료됨")
        } catch (e: SignatureException) {
            throw InvalidTokenException("유효하지 않은 토큰 시그니처")
        } catch (e: Exception) {
            throw InvalidTokenException("알 수 없는 토큰")
        }
    }
}