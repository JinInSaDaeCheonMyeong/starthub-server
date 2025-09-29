package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.StartupStatus
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserGender
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserRole
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val email: String,
    var password: String? = null,
    var username: String? = null,
    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,
    var birth: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    var gender: UserGender? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthType,
    @Column(nullable = true)
    val providerId: String? = null,
    var profileImage: String? = null,
    @Column(nullable = false)
    var deleted: Boolean = false,
    var deletedAt: LocalDateTime? = null,
    @Column(nullable = false)
    var isFirstLogin: Boolean = true,
    @Enumerated(EnumType.STRING)
    var startupStatus: StartupStatus? = null,
    var companyName: String? = null,
    @Column(columnDefinition = "TEXT")
    var companyDescription: String? = null,
    var numberOfEmployees: Int? = null,
    var companyWebsite: String? = null,
    var startupLocation: String? = null,
    var annualRevenue: Long? = null,
    var startupHistory: Int? = null,
) : BaseEntity()
