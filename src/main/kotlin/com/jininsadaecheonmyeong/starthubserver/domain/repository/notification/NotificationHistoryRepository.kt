package com.jininsadaecheonmyeong.starthubserver.domain.repository.notification

import com.jininsadaecheonmyeong.starthubserver.domain.entity.announcement.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.entity.notification.NotificationHistory
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
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

    fun findAllByUserOrderByCreatedAtDesc(user: User): List<NotificationHistory>
}
