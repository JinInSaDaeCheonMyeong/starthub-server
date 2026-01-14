package com.jininsadaecheonmyeong.starthubserver.repository.announcement

import com.jininsadaecheonmyeong.starthubserver.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.entity.announcement.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnouncementLikeRepository : JpaRepository<AnnouncementLike, Long> {
    fun findByUserAndAnnouncement(
        user: User,
        announcement: Announcement,
    ): AnnouncementLike?

    fun existsByUserAndAnnouncement(
        user: User,
        announcement: Announcement,
    ): Boolean

    fun findByUserOrderByCreatedAtDesc(
        user: User,
        pageable: Pageable,
    ): Page<AnnouncementLike>

    fun findAllByUserAndAnnouncementIn(
        user: User,
        announcements: List<Announcement>,
    ): List<AnnouncementLike>

    fun findAllByUserIdAndAnnouncementIn(
        userId: Long,
        announcements: List<Announcement>,
    ): List<AnnouncementLike>
}
