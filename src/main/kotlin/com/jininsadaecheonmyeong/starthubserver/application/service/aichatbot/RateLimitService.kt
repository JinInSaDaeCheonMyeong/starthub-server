package com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot

import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RateLimitService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val log = logger()

    fun isRateLimited(userId: Long): Boolean {
        val key = "$USER_RATE_LIMIT_PREFIX$userId"
        return try {
            checkAndIncrement(key, USER_LIMIT_PER_MINUTE)
        } catch (e: Exception) {
            log.error("Redis 연결 실패 (User Rate Limit 체크): userId={}, error={}", userId, e.message, e)
            false
        }
    }

    fun isIpRateLimited(ip: String): Boolean {
        val key = "$IP_RATE_LIMIT_PREFIX$ip"
        return try {
            checkAndIncrement(key, IP_LIMIT_PER_MINUTE)
        } catch (e: Exception) {
            log.error("Redis 연결 실패 (IP Rate Limit 체크): ip={}, error={}", ip, e.message, e)
            false
        }
    }

    fun recordViolation(userId: Long): Long {
        return try {
            val key = "$VIOLATION_PREFIX$userId"
            val count = redisTemplate.opsForValue().increment(key) ?: 1
            if (count == 1L) {
                redisTemplate.expire(key, VIOLATION_WINDOW_MINUTES, TimeUnit.MINUTES)
            }
            count
        } catch (e: Exception) {
            log.error("Redis 연결 실패 (위반 기록): userId={}, error={}", userId, e.message, e)
            0
        }
    }

    fun getViolationCount(userId: Long): Long {
        return try {
            val key = "$VIOLATION_PREFIX$userId"
            redisTemplate.opsForValue().get(key)?.toLongOrNull() ?: 0
        } catch (e: Exception) {
            log.error("Redis 연결 실패 (위반 횟수 조회): userId={}, error={}", userId, e.message, e)
            0
        }
    }

    private fun checkAndIncrement(
        key: String,
        limit: Int,
    ): Boolean {
        val count = redisTemplate.opsForValue().increment(key) ?: 1
        if (count == 1L) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS)
        }
        return count > limit
    }

    companion object {
        private const val USER_RATE_LIMIT_PREFIX = "chatbot:rate_limit:user:"
        private const val IP_RATE_LIMIT_PREFIX = "chatbot:rate_limit:ip:"
        private const val VIOLATION_PREFIX = "chatbot:rate_violations:"
        private const val WINDOW_SECONDS = 60L
        private const val VIOLATION_WINDOW_MINUTES = 60L

        const val USER_LIMIT_PER_MINUTE = 12
        const val IP_LIMIT_PER_MINUTE = 100
        const val BAN_THRESHOLD = 15
    }
}
