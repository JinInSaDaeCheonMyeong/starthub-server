package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data

data class GoogleTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String,
    val scope: String,
    val id_token: String
)