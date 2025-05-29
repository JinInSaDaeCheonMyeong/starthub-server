package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

data class OAuthResponse (
    val access: String,
    val refresh: String,
    val isFirstLogin: Boolean
)