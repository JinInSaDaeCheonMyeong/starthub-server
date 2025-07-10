package com.jininsadaecheonmyeong.starthubserver.global.security.config

import com.jininsadaecheonmyeong.starthubserver.global.security.token.properties.TokenProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TokenProperties::class)
class TokenConfig
