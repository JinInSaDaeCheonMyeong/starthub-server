package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.mapper

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import org.springframework.stereotype.Component

@Component
class AnnouncementPersistenceMapper {
    fun toDomain(entity: AnnouncementJpaEntity): Announcement {
        return Announcement(
            id = entity.id,
            title = entity.title,
            url = entity.url,
            organization = entity.organization,
            receptionPeriod = entity.receptionPeriod,
            status = entity.status,
            likeCount = entity.likeCount,
            supportField = entity.supportField,
            targetAge = entity.targetAge,
            contactNumber = entity.contactNumber,
            region = entity.region,
            organizationType = entity.organizationType,
            startupHistory = entity.startupHistory,
            departmentInCharge = entity.departmentInCharge,
            content = entity.content,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toJpaEntity(domain: Announcement): AnnouncementJpaEntity {
        return AnnouncementJpaEntity(
            id = domain.id,
            title = domain.title,
            url = domain.url,
            organization = domain.organization,
            receptionPeriod = domain.receptionPeriod,
            status = domain.status,
            likeCount = domain.likeCount,
            supportField = domain.supportField,
            targetAge = domain.targetAge ?: "",
            contactNumber = domain.contactNumber ?: "",
            region = domain.region ?: "",
            organizationType = domain.organizationType ?: "",
            startupHistory = domain.startupHistory ?: "",
            departmentInCharge = domain.departmentInCharge ?: "",
            content = domain.content
        ).apply {
            domain.createdAt?.let { this.createdAt = it }
            domain.updatedAt?.let { this.updatedAt = it }
        }
    }

    fun updateJpaEntity(entity: AnnouncementJpaEntity, domain: Announcement) {
        entity.title = domain.title
        entity.url = domain.url
        entity.organization = domain.organization
        entity.receptionPeriod = domain.receptionPeriod
        entity.status = domain.status
        entity.likeCount = domain.likeCount
        entity.supportField = domain.supportField
        entity.targetAge = domain.targetAge ?: ""
        entity.contactNumber = domain.contactNumber ?: ""
        entity.region = domain.region ?: ""
        entity.organizationType = domain.organizationType ?: ""
        entity.startupHistory = domain.startupHistory ?: ""
        entity.departmentInCharge = domain.departmentInCharge ?: ""
        entity.content = domain.content
    }
}
