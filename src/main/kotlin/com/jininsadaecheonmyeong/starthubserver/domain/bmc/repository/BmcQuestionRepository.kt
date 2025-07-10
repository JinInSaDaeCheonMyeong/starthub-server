package com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface BmcQuestionRepository : JpaRepository<BmcQuestion, UUID> {
    fun findByIdAndUser(
        id: UUID,
        user: User,
    ): Optional<BmcQuestion>

    fun findBySessionIdAndUser(
        sessionId: String,
        user: User,
    ): Optional<BmcQuestion>

    fun findAllByUserOrderByCreatedAtDesc(user: User): List<BmcQuestion>

    fun existsBySessionIdAndUser(
        sessionId: String,
        user: User,
    ): Boolean
}
