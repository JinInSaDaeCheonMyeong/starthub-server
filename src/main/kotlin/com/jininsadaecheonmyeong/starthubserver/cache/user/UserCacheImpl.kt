package com.jininsadaecheonmyeong.starthubserver.cache.user

import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.repository.user.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class UserCacheImpl(
    private val userRepository: UserRepository,
) : UserCache {
    @Cacheable(cacheNames = ["users"], key = "#userId", unless = "#result == null")
    override fun getById(userId: Long): User? = userRepository.findById(userId).orElse(null)

    @CachePut(cacheNames = ["users"], key = "#result.id", unless = "#result == null")
    override fun put(user: User): User = user

    @CacheEvict(cacheNames = ["users"], key = "#userId")
    override fun evict(userId: Long) = Unit

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    override fun clear() = Unit
}
