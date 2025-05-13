package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

interface OAuthUserInfo {
    val id: String
    val name: String
    val email: String
    val profileImage: String?
}