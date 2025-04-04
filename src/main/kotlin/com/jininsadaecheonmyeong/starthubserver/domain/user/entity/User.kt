package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import java.util.*
import jakarta.persistence.*
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.UserRole


@Entity(name = "user_tbl")
class User(
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    var password: String,

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,
)