package com.jininsadaecheonmyeong.starthubserver.domain.entity.user

import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.BusinessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "user_startup_fields",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "startup_field"])],
)
class UserStartupField(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Enumerated(EnumType.STRING)
    @Column(name = "startup_field", nullable = false)
    val businessType: BusinessType,
    @Column(name = "custom_field")
    val customField: String? = null,
)
