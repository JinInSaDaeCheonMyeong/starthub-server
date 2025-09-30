package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.AppleUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.parser.AppleTokenParser
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service

@Service
class AppleService(
    private val appleTokenParser: AppleTokenParser,
) {
    fun exchangeCodeForUserInfoApp(idToken: String): AppleUserInfo {
        val claims = appleTokenParser.parseIdToken(idToken)

        return createAppleUserInfoFromClaims(claims)
    }

    private fun createAppleUserInfoFromClaims(claims: Claims): AppleUserInfo {
        return AppleUserInfo(
            sub = claims.subject,
            name = claims["name"] as? String ?: "",
            email = claims.get("email", String::class.java),
            email_verified = claims.get("email_verified", Boolean::class.java),
            given_name = claims["given_name"] as? String,
            family_name = claims["family_name"] as? String,
            locale = claims["locale"] as? String,
        )
    }
}
