package com.jininsadaecheonmyeong.starthubserver.domain.schedule.repository

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.entity.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ScheduleRepository : JpaRepository<Schedule, Long> {
    @Query(
        "SELECT s FROM Schedule s WHERE s.user.id = :userId AND " +
            "((s.startDate BETWEEN :startDate AND :endDate) OR " +
            "(s.endDate BETWEEN :startDate AND :endDate) OR " +
            "(s.startDate <= :startDate AND s.endDate >= :endDate))",
    )
    fun findSchedulesByMonth(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<Schedule>

    fun findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        userId: Long,
        date1: LocalDate,
        date2: LocalDate,
    ): List<Schedule>
}
