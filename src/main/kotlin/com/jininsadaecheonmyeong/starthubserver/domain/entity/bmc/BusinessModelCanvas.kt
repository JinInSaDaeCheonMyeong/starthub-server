package com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.enums.bmc.BmcTemplateType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var templateType: BmcTemplateType = BmcTemplateType.STARTHUB,
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
    @Column(columnDefinition = "LONGTEXT")
    var imageUrl: String? = null,
) : BaseEntity() {
    fun isOwner(user: User): Boolean = this.user.id == user.id

    fun updateCanvas(
        title: String? = null,
        templateType: BmcTemplateType? = null,
        customerSegments: String? = null,
        valueProposition: String? = null,
        channels: String? = null,
        customerRelationships: String? = null,
        revenueStreams: String? = null,
        keyResources: String? = null,
        keyActivities: String? = null,
        keyPartners: String? = null,
        costStructure: String? = null,
        imageUrl: String? = null,
    ) {
        title?.let { this.title = it }
        templateType?.let { this.templateType = it }
        customerSegments?.let { this.customerSegments = it }
        valueProposition?.let { this.valueProposition = it }
        channels?.let { this.channels = it }
        customerRelationships?.let { this.customerRelationships = it }
        revenueStreams?.let { this.revenueStreams = it }
        keyResources?.let { this.keyResources = it }
        keyActivities?.let { this.keyActivities = it }
        keyPartners?.let { this.keyPartners = it }
        costStructure?.let { this.costStructure = it }
        imageUrl?.let { this.imageUrl = it }
    }

    fun markAsCompleted() {
        this.isCompleted = true
    }

    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted

    fun updateImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }
}
