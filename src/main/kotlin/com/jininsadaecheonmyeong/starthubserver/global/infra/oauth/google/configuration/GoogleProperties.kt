package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google.client")
class GoogleProperties {
    lateinit var id: String
    lateinit var secret: String
    lateinit var redirectUri: String
    lateinit var tokenUri: String
    lateinit var userInfoUri: String
    lateinit var grantType: String
}