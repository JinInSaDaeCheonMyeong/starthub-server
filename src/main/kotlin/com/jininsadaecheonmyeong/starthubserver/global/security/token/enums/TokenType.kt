package com.jininsadaecheonmyeong.starthubserver.global.security.token.enums

import com.jininsadaecheonmyeong.starthubserver.global.security.token.exception.InvalidTokenTypeException

enum class TokenType(
    val value: String,
) {
    ACCESS_TOKEN("access"),
    REFRESH_TOKEN("refresh"),
    ;

    companion object {
        fun toTokenType(string: String): TokenType =
            entries.find { it.value == string } ?: throw InvalidTokenTypeException()
    }
}
