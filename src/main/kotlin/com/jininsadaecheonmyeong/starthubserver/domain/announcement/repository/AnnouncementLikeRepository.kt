package com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnouncementLikeRepository : JpaRepository<AnnouncementLike, Long> {
    fun findByUserAndAnnouncement(user: User, announcement: Announcement): AnnouncementLike?
    fun existsByUserAndAnnouncement(user: User, announcement: Announcement): Boolean
}
