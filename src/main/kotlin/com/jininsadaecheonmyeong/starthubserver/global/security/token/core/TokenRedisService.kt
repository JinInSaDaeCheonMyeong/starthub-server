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
        redisTemplate.opsForValue().set("$KEY_PREFIX:$email", refreshToken, properties.refresh, TimeUnit.MILLISECONDS)
    }

    fun findByEmail(email: String): String? {
        return redisTemplate.opsForValue()["$KEY_PREFIX:$email"]
    }

    fun deleteRefreshToken(email: String) {
        redisTemplate.delete("$KEY_PREFIX:$email")
    }

    companion object {
        private const val KEY_PREFIX = "refresh_token:"
    }
}
