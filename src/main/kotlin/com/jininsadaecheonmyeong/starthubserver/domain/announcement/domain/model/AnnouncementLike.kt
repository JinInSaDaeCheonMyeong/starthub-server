package com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model

import java.time.LocalDateTime

data class AnnouncementLike(
    val id: Long? = null,
    val userId: Long,
    val announcementId: Long,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(userId > 0) { "유효하지 않은 사용자 ID입니다" }
        require(announcementId > 0) { "유효하지 않은 공고 ID입니다" }
    }

    companion object {
        fun create(userId: Long, announcementId: Long): AnnouncementLike {
            return AnnouncementLike(
                userId = userId,
                announcementId = announcementId
            )
        }
    }
}
