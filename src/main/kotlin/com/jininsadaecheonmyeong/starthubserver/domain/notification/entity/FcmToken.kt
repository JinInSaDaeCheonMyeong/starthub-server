package com.jininsadaecheonmyeong.starthubserver.domain.notification.entity

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
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
@Table(name = "fcm_tokens")
class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, unique = true, length = 512)
    var token: String,
    @Column(nullable = false)
    var deviceType: String = "UNKNOWN",
) : BaseEntity() {
    fun updateToken(newToken: String) {
        this.token = newToken
    }
}
