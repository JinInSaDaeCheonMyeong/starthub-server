package com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BmcQuestionRepository : JpaRepository<BmcQuestion, Long> {
    fun findByIdAndUser(
        id: Long,
        user: User,
    ): Optional<BmcQuestion>

    fun findAllByUserOrderByCreatedAtDesc(user: User): List<BmcQuestion>

    fun findByUserAndTitleAndIsCompletedFalse(
        user: User,
        title: String,
    ): Optional<BmcQuestion>
}
