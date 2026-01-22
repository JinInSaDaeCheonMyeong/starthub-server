package com.jininsadaecheonmyeong.starthubserver.presentation.dto.oauth2

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.AuthType

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
