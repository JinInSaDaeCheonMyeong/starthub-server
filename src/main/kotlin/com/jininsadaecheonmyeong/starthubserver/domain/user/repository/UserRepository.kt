package com.jininsadaecheonmyeong.starthubserver.domain.user.repository

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByProviderAndProviderId(
        provider: AuthType,
        providerId: String,
    ): User?

    fun existsByEmail(email: String): Boolean
}
