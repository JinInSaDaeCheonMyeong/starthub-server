package com.jininsadaecheonmyeong.starthubserver.repository.user

import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.user.AuthType
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByProviderAndProviderId(
        provider: AuthType,
        providerId: String,
    ): User?

    fun existsByEmail(email: String): Boolean
}
