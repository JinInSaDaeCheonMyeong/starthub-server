package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "bmc_modification_requests")
class BmcModificationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_bmc_id", nullable = false)
    val businessModelCanvas: BusinessModelCanvas,

    @Column(columnDefinition = "TEXT", nullable = false)
    var modificationRequest: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var requestType: BmcModificationType = BmcModificationType.MODIFY,

    @Column(nullable = false)
    var isProcessed: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var aiResponse: String? = null
) : BaseEntity() {

    fun markAsProcessed(aiResponse: String) {
        this.isProcessed = true
        this.aiResponse = aiResponse
    }

    fun isOwner(user: User): Boolean = this.user.id == user.id
}

enum class BmcModificationType {
    MODIFY,
    REGENERATE
}