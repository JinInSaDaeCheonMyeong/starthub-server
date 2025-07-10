package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth")
data class OAuthProperties(
    val frontRedirectUri: String
)