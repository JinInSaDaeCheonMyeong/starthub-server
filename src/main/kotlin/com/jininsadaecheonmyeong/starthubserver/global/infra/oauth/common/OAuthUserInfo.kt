package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserRole

interface OAuthUserInfo {
    val sub: String
    val name: String
    val email: String

    fun toUser(provider: AuthType): User =
        User(
            email = email,
            role = UserRole.USER,
            provider = provider,
            providerId = sub,
        )
}
