package com.jininsadaecheonmyeong.starthubserver.domain.email.entity

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Email(
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var email: String? = null,

    @Column(length = 6)
    var verificationCode: String? = null,

    val expirationDate: LocalDateTime = LocalDateTime.now().plusMinutes(5),

    @Column(nullable = false)
    var isVerified: Boolean = false
) : BaseEntity()