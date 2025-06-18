package com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.enums.RecruitRole
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "recruit_positions")
class RecruitPosition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: RecruitRole,

    @Column(nullable = false)
    var count: Int,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false)
    var closed: Boolean = false
) : BaseEntity()