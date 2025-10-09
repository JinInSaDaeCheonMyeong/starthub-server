package com.jininsadaecheonmyeong.starthubserver.domain.notification.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.notification.entity.NotificationHistory
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationHistoryRepository : JpaRepository<NotificationHistory, Long> {
    fun existsByUserAndAnnouncementAndNotificationType(
        user: User,
        announcement: Announcement,
        notificationType: String,
    ): Boolean

    fun findAllByUserAndNotificationType(
        user: User,
        notificationType: String,
    ): List<NotificationHistory>
}
