package com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "naver.client")
class NaverProperties {
    lateinit var id: String
    lateinit var secret: String
    lateinit var redirectUri: String
    lateinit var tokenUri: String
    lateinit var userInfoUri: String
    lateinit var grantType: String
}