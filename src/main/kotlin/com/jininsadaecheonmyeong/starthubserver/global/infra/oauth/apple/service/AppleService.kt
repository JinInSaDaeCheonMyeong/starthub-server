package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service

import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.data.AppleUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.parser.AppleTokenParser
import org.springframework.stereotype.Service

@Service
class AppleService(
    private val appleTokenParser: AppleTokenParser,
) {
    fun exchangeCodeForUserInfoApp(idToken: String): AppleUserInfo {
        val map = appleTokenParser.parseIdToken(idToken)
        return AppleUserInfo(
            sub = map["sub"] as? String ?: "",
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            email_verified = map["email_verified"] as? Boolean ?: false,
            given_name = map["given_name"] as? String,
            family_name = map["family_name"] as? String,
            locale = map["locale"] as? String,
        )
    }
}
