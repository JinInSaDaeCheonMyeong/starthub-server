package com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: AnnouncementStatus = AnnouncementStatus.ACTIVE,
    @Column(name = "like_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0 CHECK (like_count >= 0)")
    var likeCount: Int = 0,
    @Column(name = "support_field")
    var supportField: String,
    @Column(name = "target_age")
    var targetAge: String,
    @Column(name = "contact_number")
    var contactNumber: String,
    @Column(name = "region")
    var region: String,
    @Column(name = "organization_type")
    var organizationType: String,
    @Column(name = "startup_history")
    var startupHistory: String,
    @Column(name = "department_in_charge")
    var departmentInCharge: String,
    @Column(columnDefinition = "TEXT")
    var content: String,
) : BaseEntity()
