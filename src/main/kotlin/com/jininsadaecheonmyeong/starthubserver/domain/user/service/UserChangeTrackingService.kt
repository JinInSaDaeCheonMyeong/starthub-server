package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class UserChangeTrackingService(
    private val userRepository: UserRepository,
    private val fastApiIntegrationService: FastApiIntegrationService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    private val logger = LoggerFactory.getLogger(UserChangeTrackingService::class.java)
    
    companion object {
        private const val REDIS_KEY_PREFIX = "user_change_tracking:"
        private const val LAST_UPDATE_KEY = "last_update_time"
    }
    
    fun markUserForUpdate(userId: Long, changeType: String) {
        try {
            val now = LocalDateTime.now()
            val key = "$REDIS_KEY_PREFIX$userId"
            
            // Redis에 사용자 변경 사항 기록
            redisTemplate.opsForHash<String, String>().putAll(key, mapOf(
                "user_id" to userId.toString(),
                "change_type" to changeType,
                "changed_at" to now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "needs_update" to "true"
            ))
            
            // 1시간 후 만료 (스케줄러가 처리하면 자동 삭제)
            redisTemplate.expire(key, java.time.Duration.ofHours(2))
            
            logger.info("사용자 변경 사항 기록: user_id=$userId, change_type=$changeType")
            
        } catch (e: Exception) {
            logger.error("사용자 변경 사항 기록 실패: user_id=$userId", e)
        }
    }
    
    @Transactional
    fun processChangedUsers(): Int {
        return try {
            val pattern = "$REDIS_KEY_PREFIX*"
            val keys = redisTemplate.keys(pattern)
            
            if (keys.isNullOrEmpty()) {
                logger.info("변경된 사용자가 없습니다.")
                return 0
            }
            
            logger.info("${keys.size}명의 사용자 변경 사항을 처리합니다.")
            
            var successCount = 0
            
            keys.forEach { key ->
                try {
                    val userDataMap = redisTemplate.opsForHash<String, String>().entries(key)
                    val userId = userDataMap["user_id"]?.toLongOrNull()
                    val needsUpdate = userDataMap["needs_update"]
                    
                    if (userId != null && needsUpdate == "true") {
                        // 사용자 데이터를 FastAPI로 전송
                        val success = fastApiIntegrationService.sendUserInterestsToFastApi(userId)
                        
                        if (success) {
                            // 성공 시 Redis에서 제거
                            redisTemplate.delete(key)
                            successCount++
                            logger.info("사용자 데이터 업데이트 완료: user_id=$userId")
                        } else {
                            logger.error("사용자 데이터 업데이트 실패: user_id=$userId")
                            // 실패 시 재시도를 위해 변경 시간만 업데이트
                            redisTemplate.opsForHash<String, String>().put(key, "retry_count", 
                                (userDataMap["retry_count"]?.toIntOrNull() ?: 0).plus(1).toString())
                        }
                    }
                    
                } catch (e: Exception) {
                    logger.error("사용자 변경 사항 처리 중 오류: key=$key", e)
                }
            }
            
            // 마지막 업데이트 시간 기록
            redisTemplate.opsForValue().set(
                LAST_UPDATE_KEY, 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
            logger.info("사용자 변경 사항 처리 완료: $successCount/${keys.size}")
            successCount
            
        } catch (e: Exception) {
            logger.error("사용자 변경 사항 일괄 처리 실패", e)
            0
        }
    }
    
    fun getChangedUsersCount(): Int {
        return try {
            val pattern = "$REDIS_KEY_PREFIX*"
            redisTemplate.keys(pattern)?.size ?: 0
        } catch (e: Exception) {
            logger.error("변경된 사용자 수 조회 실패", e)
            0
        }
    }
    
    fun getLastUpdateTime(): String? {
        return try {
            redisTemplate.opsForValue().get(LAST_UPDATE_KEY)
        } catch (e: Exception) {
            logger.error("마지막 업데이트 시간 조회 실패", e)
            null
        }
    }
    
    fun forceUpdateUser(userId: Long): Boolean {
        return try {
            val success = fastApiIntegrationService.sendUserInterestsToFastApi(userId)
            
            if (success) {
                // 성공 시 Redis에서 해당 사용자 제거
                val key = "$REDIS_KEY_PREFIX$userId"
                redisTemplate.delete(key)
                logger.info("사용자 강제 업데이트 완료: user_id=$userId")
            }
            
            success
        } catch (e: Exception) {
            logger.error("사용자 강제 업데이트 실패: user_id=$userId", e)
            false
        }
    }
}