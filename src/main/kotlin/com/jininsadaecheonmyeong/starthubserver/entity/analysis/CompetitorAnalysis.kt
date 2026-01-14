package com.jininsadaecheonmyeong.starthubserver.entity.analysis

import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
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

@Entity
@Table(name = "competitor_analysis")
class CompetitorAnalysis(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_bmc_id", nullable = false)
    val businessModelCanvas: BusinessModelCanvas,
    @Column(columnDefinition = "TEXT")
    var userBmcSummary: String? = null,
    @Column(columnDefinition = "TEXT")
    var userScaleAnalysis: String? = null,
    @Column(columnDefinition = "TEXT")
    var strengthsAnalysis: String? = null,
    @Column(columnDefinition = "TEXT")
    var weaknessesAnalysis: String? = null,
    @Column(columnDefinition = "TEXT")
    var globalExpansionStrategy: String? = null,
    @Column(nullable = false)
    var deleted: Boolean = false,
) : BaseEntity() {
    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted
}
