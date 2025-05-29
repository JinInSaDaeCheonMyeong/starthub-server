package com.jininsadaecheonmyeong.starthubserver.domain.user.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.InterestType
import jakarta.persistence.*

@Entity
@Table(
    name = "user_interests",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "interest"])]
)
class UserInterest (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false)
    val interestType: InterestType
)