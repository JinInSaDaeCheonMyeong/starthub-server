package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.apple")
data class AppleProperties(
    val teamId: String,
    val clientId: String,
    val keyId: String,
    val privateKey: String,
)
