package com.jininsadaecheonmyeong.starthubserver.domain.user.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User

data class UserRequest(
    val email: String,
    val password: String,
) {
    fun toEntity(encodedPassword: String): User {
        return User(
            email = email,
            password = encodedPassword,
        )
    }
}