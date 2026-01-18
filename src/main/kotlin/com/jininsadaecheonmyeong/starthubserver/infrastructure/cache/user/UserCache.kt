package com.jininsadaecheonmyeong.starthubserver.infrastructure.cache.user

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User

interface UserCache {
    fun getById(userId: Long): User?

    fun put(user: User): User

    fun evict(userId: Long)

    fun clear()
}
