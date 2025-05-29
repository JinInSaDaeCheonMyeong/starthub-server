package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserRole
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    var password: String? = null,

    var username: String? = null,

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthType,

    @Column(nullable = true)
    val providerId: String? = null,

    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity()