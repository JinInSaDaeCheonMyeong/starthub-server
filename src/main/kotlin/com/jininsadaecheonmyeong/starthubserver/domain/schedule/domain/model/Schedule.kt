package com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Schedule Domain Model
 * - JPA 의존성 제거 (순수 Kotlin)
 * - User, Announcement를 ID로 참조
 */
data class Schedule(
    val id: Long? = null,
    val userId: Long,
    val announcementId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(userId > 0) { "유효하지 않은 사용자 ID입니다" }
        require(announcementId > 0) { "유효하지 않은 공고 ID입니다" }
        require(!startDate.isAfter(endDate)) { "시작일은 종료일보다 이전이어야 합니다" }
    }

    companion object {
        fun create(
            userId: Long,
            announcementId: Long,
            startDate: LocalDate,
            endDate: LocalDate
        ): Schedule {
            return Schedule(
                userId = userId,
                announcementId = announcementId,
                startDate = startDate,
                endDate = endDate,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}
