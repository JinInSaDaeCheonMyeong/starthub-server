package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenValidator(
    private val parser: TokenParser,
) {
    fun validateAll(token: String, tokenType: TokenType) {
        validate(token)
        validateType(token, tokenType)
    }

    fun validateType(token: String, tokenType: TokenType) {
        if (parser.findType(token) != tokenType) throw RuntimeException("token mismatch")
    }

    fun validate(token: String) {
        try {
            parser.findType(token)
            if (
                parser.findExpiration(token).before(Date())
            ) throw RuntimeException("토큰 만료됨")
        } catch (e: ExpiredJwtException) {
            throw RuntimeException("토큰 만료됨")
        } catch (e: SignatureException) {
            throw RuntimeException("유효하지 않은 토큰 시그니처")
        } catch (e: Exception) {
            throw RuntimeException("알 수 없는 토큰")
        }
    }
}