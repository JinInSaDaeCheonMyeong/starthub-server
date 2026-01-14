package com.jininsadaecheonmyeong.starthubserver.cache.user

import com.jininsadaecheonmyeong.starthubserver.entity.user.User

interface UserCache {
    fun getById(userId: Long): User?

    fun put(user: User): User

    fun evict(userId: Long)

    fun clear()
}
