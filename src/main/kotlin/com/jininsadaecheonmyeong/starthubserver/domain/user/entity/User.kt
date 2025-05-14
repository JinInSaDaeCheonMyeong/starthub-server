package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.UserRole
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "user_tbl")
class User (
    @field:Id
    @field:Column(nullable = false, unique = true)
    @field:GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @field:Column(nullable = false, unique = true)
    val email: String,

    var password: String? = null,

    @field:Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    var provider: AuthProvider,

    @field:Column(nullable = true)
    var providerId: String? = null,

    @field:Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity()