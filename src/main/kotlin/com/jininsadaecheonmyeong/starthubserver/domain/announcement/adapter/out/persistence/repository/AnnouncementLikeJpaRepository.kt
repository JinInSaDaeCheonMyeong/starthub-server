package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementLikeJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AnnouncementLikeJpaRepository : JpaRepository<AnnouncementLikeJpaEntity, Long> {
    fun findByUserAndAnnouncement(
        user: User,
        announcement: AnnouncementJpaEntity,
    ): AnnouncementLikeJpaEntity?

    fun existsByUserAndAnnouncement(
        user: User,
        announcement: AnnouncementJpaEntity,
    ): Boolean

    fun findByUserOrderByCreatedAtDesc(
        user: User,
        pageable: Pageable,
    ): Page<AnnouncementLikeJpaEntity>

    fun findAllByUserAndAnnouncementIn(
        user: User,
        announcements: List<AnnouncementJpaEntity>,
    ): List<AnnouncementLikeJpaEntity>

    fun findAllByUserIdAndAnnouncementIn(
        userId: Long,
        announcements: List<AnnouncementJpaEntity>,
    ): List<AnnouncementLikeJpaEntity>

    fun findByUserIdAndAnnouncementId(userId: Long, announcementId: Long): AnnouncementLikeJpaEntity?

    fun existsByUserIdAndAnnouncementId(userId: Long, announcementId: Long): Boolean
}
