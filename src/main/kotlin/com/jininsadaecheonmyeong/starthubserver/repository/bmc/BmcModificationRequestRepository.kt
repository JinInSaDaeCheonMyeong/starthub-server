package com.jininsadaecheonmyeong.starthubserver.repository.bmc

import com.jininsadaecheonmyeong.starthubserver.dto.request.bmc.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BmcModificationRequestRepository : JpaRepository<BmcModificationRequest, Long> {
    fun findByBusinessModelCanvasAndUserOrderByCreatedAtDesc(
        bmc: BusinessModelCanvas,
        user: User,
    ): List<BmcModificationRequest>

    fun findByUserOrderByCreatedAtDesc(user: User): List<BmcModificationRequest>

    fun findByIdAndUser(
        id: Long,
        user: User,
    ): Optional<BmcModificationRequest>
}
