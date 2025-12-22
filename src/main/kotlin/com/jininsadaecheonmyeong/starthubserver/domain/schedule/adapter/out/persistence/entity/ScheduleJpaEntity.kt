package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.entity

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "schedules")
class ScheduleJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    val announcement: Announcement,

    val startDate: LocalDate,
    val endDate: LocalDate
) : BaseEntity()
