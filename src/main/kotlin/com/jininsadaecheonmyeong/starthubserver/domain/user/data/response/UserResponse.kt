package com.jininsadaecheonmyeong.starthubserver.domain.user.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
import java.time.LocalDate

data class UserResponse(
    val email: String,
    val username: String?,
    val birth: LocalDate?,
    val gender: UserGender?,
    val profileImage: String?,
) {
    constructor(user: User) : this(
        email = user.email,
        username = user.username,
        birth = user.birth,
        gender = user.gender,
        profileImage = user.profileImage,
    )
}
