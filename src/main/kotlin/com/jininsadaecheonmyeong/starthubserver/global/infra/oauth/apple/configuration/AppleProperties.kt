package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.apple")
class AppleProperties {
    lateinit var teamId: String
    lateinit var clientId: String
    lateinit var keyId: String
    lateinit var privateKey: String
    lateinit var redirectUri: String
    lateinit var tokenUri: String
    lateinit var grantType: String
}