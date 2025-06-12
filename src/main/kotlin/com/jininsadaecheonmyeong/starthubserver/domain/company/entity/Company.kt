package com.jininsadaecheonmyeong.starthubserver.domain.company.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "companies")
class Company (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val companyName: String,

    @Column(nullable = false)
    val companyDescription: String,

    @Column(nullable = false)
    val companyCategory: BusinessType,

    @Column(nullable = false)
    val businessDescription: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val contactEmail: String? = null,

    @Column(nullable = false)
    val contactNumber: String? = null,

    val address: String? = null,

    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity()