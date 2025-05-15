package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
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

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider,

    @Column(nullable = true)
    var providerId: String? = null,

    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity()