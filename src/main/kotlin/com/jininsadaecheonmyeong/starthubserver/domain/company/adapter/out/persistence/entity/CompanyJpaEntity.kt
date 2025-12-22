package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*

/**
 * Company JPA Entity
 * - Persistence Layer 전용 (Adapter)
 * - Domain Model과 분리
 */
@Entity
@Table(name = "companies")
class CompanyJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var companyName: String,

    @Column(nullable = false)
    var companyDescription: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var companyCategory: BusinessType,

    @Column(nullable = false)
    var businessDescription: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_founder_id", nullable = false)
    var founder: User,

    var companyUrl: String? = null,

    @Column(nullable = false)
    var contactEmail: String,

    @Column(nullable = false)
    var contactNumber: String,

    var address: String? = null,

    @Column(nullable = false)
    var employeeCount: Int,

    var logoImage: String? = null,

    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseEntity()
