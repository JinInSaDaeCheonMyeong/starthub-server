package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider

interface OAuthUserInfo {
    val id: String
    val name: String
    val email: String
    val profileImage: String?
}