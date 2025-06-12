package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.naver")
data class NaverProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val androidClientId: String,
    val androidRedirectUri: String,
    val iosClientId: String,
    val iosRedirectUri: String,
    val tokenUri: String,
    val userInfoUri: String,
    val grantType: String
)