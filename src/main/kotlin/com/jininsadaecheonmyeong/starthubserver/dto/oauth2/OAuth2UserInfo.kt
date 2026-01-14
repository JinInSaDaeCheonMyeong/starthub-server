package com.jininsadaecheonmyeong.starthubserver.dto.oauth2

import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.user.AuthType

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
