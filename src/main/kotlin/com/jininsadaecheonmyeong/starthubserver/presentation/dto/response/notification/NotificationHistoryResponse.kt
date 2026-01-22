package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.notification

import com.jininsadaecheonmyeong.starthubserver.domain.entity.notification.NotificationHistory
import java.time.LocalDateTime

data class NotificationHistoryResponse(
    val id: Long,
    val announcementId: Long,
    val announcementTitle: String,
    val notificationType: String,
    val title: String,
    val body: String,
    val isSent: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(history: NotificationHistory): NotificationHistoryResponse {
            return NotificationHistoryResponse(
                id = history.id ?: 0L,
                announcementId = history.announcement.id ?: 0L,
                announcementTitle = history.announcement.title,
                notificationType = history.notificationType,
                title = history.title,
                body = history.body,
                isSent = history.isSent,
                createdAt = history.createdAt,
            )
        }
    }
}
