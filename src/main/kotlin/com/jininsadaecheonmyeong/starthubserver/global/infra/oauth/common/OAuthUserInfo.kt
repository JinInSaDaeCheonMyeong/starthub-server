package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.UserRole

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
