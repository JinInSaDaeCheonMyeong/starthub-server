package com.jininsadaecheonmyeong.starthubserver.domain.user.data

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider
import jakarta.validation.constraints.Email

data class UserRequest(
    @field:Email(message = "올바르지 않은 이메일 형식")
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