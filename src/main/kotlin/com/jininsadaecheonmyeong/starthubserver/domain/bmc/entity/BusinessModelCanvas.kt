package com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "business_model_canvas")
class BusinessModelCanvas(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var keyPartners: String? = null,

    @Column(columnDefinition = "TEXT")
    var keyActivities: String? = null,

    @Column(columnDefinition = "TEXT")
    var keyResources: String? = null,

    @Column(columnDefinition = "TEXT")
    var valueProposition: String? = null,

    @Column(columnDefinition = "TEXT")
    var customerRelationships: String? = null,

    @Column(columnDefinition = "TEXT")
    var channels: String? = null,

    @Column(columnDefinition = "TEXT")
    var customerSegments: String? = null,

    @Column(columnDefinition = "TEXT")
    var costStructure: String? = null,

    @Column(columnDefinition = "TEXT")
    var revenueStreams: String? = null,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @Column(nullable = false)
    var deleted: Boolean = false,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_bmc_question_id")
    var bmcQuestion: BmcQuestion? = null
) : BaseEntity() {

    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun updateCanvas(
        title: String? = null,
        keyPartners: String? = null,
        keyActivities: String? = null,
        keyResources: String? = null,
        valueProposition: String? = null,
        customerRelationships: String? = null,
        channels: String? = null,
        customerSegments: String? = null,
        costStructure: String? = null,
        revenueStreams: String? = null
    ) {
        title?.let { this.title = it }
        keyPartners?.let { this.keyPartners = it }
        keyActivities?.let { this.keyActivities = it }
        keyResources?.let { this.keyResources = it }
        valueProposition?.let { this.valueProposition = it }
        customerRelationships?.let { this.customerRelationships = it }
        channels?.let { this.channels = it }
        customerSegments?.let { this.customerSegments = it }
        costStructure?.let { this.costStructure = it }
        revenueStreams?.let { this.revenueStreams = it }
    }

    fun markAsCompleted() {
        this.isCompleted = true
    }

    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted
}