package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.repository

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.entity.ScheduleJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ScheduleJpaRepository : JpaRepository<ScheduleJpaEntity, Long> {
    @Query(
        "SELECT s FROM ScheduleJpaEntity s " +
            "LEFT JOIN FETCH s.user " +
            "LEFT JOIN FETCH s.announcement " +
            "WHERE s.user.id = :userId AND " +
            "((s.startDate BETWEEN :startDate AND :endDate) OR " +
            "(s.endDate BETWEEN :startDate AND :endDate) OR " +
            "(s.startDate <= :startDate AND s.endDate >= :endDate))"
    )
    fun findSchedulesByMonth(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ScheduleJpaEntity>

    @Query(
        "SELECT s FROM ScheduleJpaEntity s " +
            "LEFT JOIN FETCH s.user " +
            "LEFT JOIN FETCH s.announcement " +
            "WHERE s.user.id = :userId " +
            "AND s.startDate <= :date " +
            "AND s.endDate >= :date"
    )
    fun findByUserIdAndDate(
        @Param("userId") userId: Long,
        @Param("date") date: LocalDate
    ): List<ScheduleJpaEntity>

    fun deleteByAnnouncementIdAndUserId(announcementId: Long, userId: Long)

    fun findAllByUser(user: User): List<ScheduleJpaEntity>
}
