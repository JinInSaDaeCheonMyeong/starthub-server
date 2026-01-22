package com.jininsadaecheonmyeong.starthubserver.presentation.dto.oauth2

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    private val oAuth2User: OAuth2User,
    val user: User,
    val isFirstLogin: Boolean,
) : OAuth2User {
    override fun getName(): String {
        return user.id.toString()
    }

    override fun getAttributes(): Map<String, Any> {
        return oAuth2User.attributes
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }
}
