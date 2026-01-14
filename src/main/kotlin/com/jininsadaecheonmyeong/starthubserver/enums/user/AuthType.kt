package com.jininsadaecheonmyeong.starthubserver.enums.user

import com.jininsadaecheonmyeong.starthubserver.dto.oauth2.OAuth2UserInfo

enum class AuthType(private val extractor: (Map<String, Any>) -> OAuth2UserInfo) {
    LOCAL({ throw IllegalStateException("LOCAL provider does not support OAuth2") }),
    GOOGLE({
        OAuth2UserInfo(
            providerId = it["sub"] as String,
            name = it["name"] as String,
            email = it["email"] as String,
            picture = it["picture"] as String?,
        )
    }),
    NAVER({
        val response = it["response"] as Map<*, *>
        OAuth2UserInfo(
            providerId = response["id"] as String,
            name = response["name"] as String,
            email = response["email"] as String,
            picture = response["profile_image"] as String?,
        )
    }),
    APPLE({
        OAuth2UserInfo(
            providerId = it["sub"] as String,
            name = it["name"] as String? ?: (it["email"] as String).split("@")[0],
            email = it["email"] as String,
            picture = null,
        )
    }),
    ;

    fun extractUserInfo(attributes: Map<String, Any>): OAuth2UserInfo {
        return this.extractor(attributes)
    }
}
