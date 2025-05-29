package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

interface OAuthUserInfo {
    val sub: String
    val name: String
    val email: String
}