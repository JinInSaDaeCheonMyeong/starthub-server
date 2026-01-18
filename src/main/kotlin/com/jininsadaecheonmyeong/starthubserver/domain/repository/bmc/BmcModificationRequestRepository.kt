package com.jininsadaecheonmyeong.starthubserver.domain.repository.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.BmcModificationRequest
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
