package com.jininsadaecheonmyeong.starthubserver.global.security.token.core

import com.jininsadaecheonmyeong.starthubserver.global.security.token.properties.TokenProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TokenRedisService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: TokenProperties,
) {
    fun storeRefreshToken(
        email: String,
        refreshToken: String,
    ) {
        redisTemplate.opsForValue().set("refreshToken:$email", refreshToken, properties.refresh, TimeUnit.MILLISECONDS)
    }

    fun findByEmail(email: String): String? {
        return redisTemplate.opsForValue()["refreshToken:$email"]
    }

    fun deleteRefreshToken(email: String) {
        redisTemplate.delete("refreshToken:$email")
    }
}
