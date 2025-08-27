package com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity

import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "announcements")
class Announcement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "title", nullable = false, length = 512)
    val title: String,
    @Column(name = "url", nullable = false, length = 1024, unique = true)
    val url: String,
    @Column(name = "organization", nullable = false)
    val organization: String,
    @Column(name = "reception_period", nullable = false)
    val receptionPeriod: String,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,
) : BaseEntity()
