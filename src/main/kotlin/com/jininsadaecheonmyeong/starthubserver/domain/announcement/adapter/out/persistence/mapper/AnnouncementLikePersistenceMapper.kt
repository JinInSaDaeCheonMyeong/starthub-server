package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.mapper

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementLikeJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.stereotype.Component

@Component
class AnnouncementLikePersistenceMapper {
    fun toDomain(entity: AnnouncementLikeJpaEntity): AnnouncementLike {
        return AnnouncementLike(
            id = entity.id,
            userId = entity.user.id!!,
            announcementId = entity.announcement.id!!,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toJpaEntity(
        domain: AnnouncementLike,
        user: User,
        announcement: AnnouncementJpaEntity
    ): AnnouncementLikeJpaEntity {
        return AnnouncementLikeJpaEntity(
            id = domain.id,
            user = user,
            announcement = announcement
        ).apply {
            domain.createdAt?.let { this.createdAt = it }
            domain.updatedAt?.let { this.updatedAt = it }
        }
    }
}
