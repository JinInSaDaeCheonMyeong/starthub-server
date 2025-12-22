package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.mapper

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.entity.ScheduleJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.stereotype.Component

@Component
class SchedulePersistenceMapper {

    fun toDomain(entity: ScheduleJpaEntity): Schedule {
        return Schedule(
            id = entity.id,
            userId = entity.user.id!!,
            announcementId = entity.announcement.id!!,
            startDate = entity.startDate,
            endDate = entity.endDate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toJpaEntity(domain: Schedule, user: User, announcement: Announcement): ScheduleJpaEntity {
        return ScheduleJpaEntity(
            id = domain.id,
            user = user,
            announcement = announcement,
            startDate = domain.startDate,
            endDate = domain.endDate
        ).apply {
            domain.createdAt?.let { this.createdAt = it }
            domain.updatedAt?.let { this.updatedAt = it }
        }
    }
}
