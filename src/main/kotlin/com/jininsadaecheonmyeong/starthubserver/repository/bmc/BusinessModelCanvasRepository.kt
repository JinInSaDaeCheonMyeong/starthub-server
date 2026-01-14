package com.jininsadaecheonmyeong.starthubserver.repository.bmc

import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BmcQuestion
import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BusinessModelCanvasRepository : JpaRepository<BusinessModelCanvas, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<BusinessModelCanvas>

    fun findAllByUserAndDeletedFalse(user: User): List<BusinessModelCanvas>

    fun findTop3ByUserAndDeletedFalseOrderByCreatedAtDesc(user: User): List<BusinessModelCanvas>

    fun findAllByDeletedFalse(): List<BusinessModelCanvas>

    fun existsByIdAndDeletedFalse(id: Long): Boolean

    fun findByBmcQuestionAndUserAndDeletedFalse(
        bmcQuestion: BmcQuestion,
        user: User,
    ): Optional<BusinessModelCanvas>
}
