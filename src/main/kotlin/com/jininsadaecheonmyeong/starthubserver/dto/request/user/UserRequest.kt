package com.jininsadaecheonmyeong.starthubserver.dto.request.user

import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.user.AuthType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class UserRequest(
    @field:Email(message = "올바르지 않은 이메일 형식")
    val email: String,
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
        message = "비밀번호는 8~16자리이며, 문자, 숫자, 특수문자를 모두 포함해야 합니다.",
    )
    val password: String,
) {
    fun toEntity(password: String): User {
        return User(
            email = email,
            password = password,
            provider = AuthType.LOCAL,
        )
    }
}
