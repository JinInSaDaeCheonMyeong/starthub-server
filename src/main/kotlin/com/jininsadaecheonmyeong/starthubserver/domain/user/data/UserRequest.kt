package com.jininsadaecheonmyeong.starthubserver.domain.user.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider

data class UserRequest(
    val email: String,
    val password: String,
) {
    fun toEntity(encodedPassword: String): User {
        return User(
            email = email,
            password = encodedPassword,
            provider = AuthProvider.LOCAL
        )
    }
}