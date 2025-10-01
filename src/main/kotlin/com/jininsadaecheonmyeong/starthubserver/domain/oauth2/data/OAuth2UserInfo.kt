package com.jininsadaecheonmyeong.starthubserver.domain.oauth2.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType

data class OAuth2UserInfo(
    val providerId: String,
    val email: String,
    val name: String,
    val picture: String?,
) {
    fun toEntity(provider: AuthType): User {
        return User(
            username = name,
            email = email,
            profileImage = picture,
            provider = provider,
            providerId = providerId,
            isFirstLogin = true,
        )
    }
}
