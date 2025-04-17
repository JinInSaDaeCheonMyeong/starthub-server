package com.jininsadaecheonmyeong.starthubserver.global.configuration.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class RedisProperties(
    @Value("\${spring.data.redis.host}") val host: String,
    @Value("\${spring.data.redis.port}") val port : Int,
    @Value("\${spring.data.redis.password}") val password: String
)