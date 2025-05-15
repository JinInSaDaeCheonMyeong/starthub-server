package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.naver")
class NaverProperties {
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var redirectUri: String
    lateinit var tokenUri: String
    lateinit var userInfoUri: String
    lateinit var grantType: String
}