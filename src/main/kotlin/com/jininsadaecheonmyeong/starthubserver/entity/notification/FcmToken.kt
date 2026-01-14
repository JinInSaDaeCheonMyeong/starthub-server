package com.jininsadaecheonmyeong.starthubserver.entity.notification

import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.notification.DeviceType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "fcm_tokens",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_device_type",
            columnNames = ["user_id", "device_type"],
        ),
    ],
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
        Index(name = "idx_token", columnList = "token"),
    ],
)
class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, length = 512)
    var token: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    var deviceType: DeviceType = DeviceType.UNKNOWN,
) : BaseEntity() {
    fun updateToken(newToken: String) {
        this.token = newToken
    }
}
