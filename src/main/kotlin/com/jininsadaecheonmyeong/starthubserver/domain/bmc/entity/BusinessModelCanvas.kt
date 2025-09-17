package com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "business_model_canvas")
class BusinessModelCanvas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var customerSegments: String? = null,
    @Column(columnDefinition = "TEXT")
    var valueProposition: String? = null,
    @Column(columnDefinition = "TEXT")
    var channels: String? = null,
    @Column(columnDefinition = "TEXT")
    var customerRelationships: String? = null,
    @Column(columnDefinition = "TEXT")
    var revenueStreams: String? = null,
    @Column(columnDefinition = "TEXT")
    var keyResources: String? = null,
    @Column(columnDefinition = "TEXT")
    var keyActivities: String? = null,
    @Column(columnDefinition = "TEXT")
    var keyPartners: String? = null,
    @Column(columnDefinition = "TEXT")
    var costStructure: String? = null,
    @Column(nullable = false)
    var isCompleted: Boolean = false,
    @Column(nullable = false)
    var deleted: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_bmc_question_id")
    var bmcQuestion: BmcQuestion? = null,
) : BaseEntity() {
    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun updateCanvas(
        title: String? = null,
        customerSegments: String? = null,
        valueProposition: String? = null,
        channels: String? = null,
        customerRelationships: String? = null,
        revenueStreams: String? = null,
        keyResources: String? = null,
        keyActivities: String? = null,
        keyPartners: String? = null,
        costStructure: String? = null,
    ) {
        title?.let { this.title = it }
        customerSegments?.let { this.customerSegments = it }
        valueProposition?.let { this.valueProposition = it }
        channels?.let { this.channels = it }
        customerRelationships?.let { this.customerRelationships = it }
        revenueStreams?.let { this.revenueStreams = it }
        keyResources?.let { this.keyResources = it }
        keyActivities?.let { this.keyActivities = it }
        keyPartners?.let { this.keyPartners = it }
        costStructure?.let { this.costStructure = it }
    }

    fun markAsCompleted() {
        this.isCompleted = true
    }

    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted
}
