package com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BusinessModelCanvasRepository : JpaRepository<BusinessModelCanvas, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<BusinessModelCanvas>

    fun findAllByUserAndDeletedFalse(user: User): List<BusinessModelCanvas>

    fun findAllByDeletedFalse(): List<BusinessModelCanvas>

    fun existsByIdAndDeletedFalse(id: Long): Boolean

    fun findByBmcQuestionAndUserAndDeletedFalse(
        bmcQuestion: BmcQuestion,
        user: User,
    ): Optional<BusinessModelCanvas>
}
