package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.data

class NaverTokenResponse {
    lateinit var access_token: String
    var refresh_token: String? = null
    lateinit var token_type: String
    lateinit var expires_in: String
    lateinit var scope: String
}