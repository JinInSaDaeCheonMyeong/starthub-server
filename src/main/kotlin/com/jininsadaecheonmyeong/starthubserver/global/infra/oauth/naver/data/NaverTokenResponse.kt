package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data

data class NaverTokenResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val token_type: String,
    val expires_in: String,
    val scope: String
)