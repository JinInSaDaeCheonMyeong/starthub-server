package com.jininsadaecheonmyeong.starthubserver.domain.email.entity

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "emails")
class Email(
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var email: String? = null,
    @Column(length = 6)
    var verificationCode: String? = null,
    var expirationDate: LocalDateTime = LocalDateTime.now().plusMinutes(5),
    @Column(nullable = false)
    var isVerified: Boolean = false,
) : BaseEntity()
