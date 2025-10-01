package com.jininsadaecheonmyeong.starthubserver.domain.user.cache

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User

interface UserCache {
    fun getById(userId: Long): User?

    fun put(user: User): User

    fun evict(userId: Long)

    fun clear()
}
