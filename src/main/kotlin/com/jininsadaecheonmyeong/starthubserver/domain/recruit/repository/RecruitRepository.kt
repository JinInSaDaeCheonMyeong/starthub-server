
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.Recruit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface RecruitRepository : JpaRepository<Recruit, Long> {
    fun findAllByDeletedFalse(pageable: Pageable): Page<Recruit>

    @Query(
        "SELECT r FROM Recruit r " +
            "LEFT JOIN FETCH r.writer " +
            "LEFT JOIN FETCH r.company " +
            "WHERE r.id = :id AND r.deleted = false",
    )
    fun findByIdAndDeletedFalse(id: Long): Optional<Recruit>
}
