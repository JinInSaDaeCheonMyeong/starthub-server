package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.google")
class GoogleProperties {
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var redirectUri: String
    lateinit var tokenUri: String
    lateinit var userInfoUri: String
    lateinit var grantType: String
}