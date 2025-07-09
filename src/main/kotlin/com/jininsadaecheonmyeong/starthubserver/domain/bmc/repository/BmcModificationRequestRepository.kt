package com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BmcModificationRequestRepository : JpaRepository<BmcModificationRequest, UUID> {
    fun findByBusinessModelCanvasAndUserOrderByCreatedAtDesc(bmc: BusinessModelCanvas, user: User): List<BmcModificationRequest>
    fun findByUserOrderByCreatedAtDesc(user: User): List<BmcModificationRequest>
    fun findByIdAndUser(id: UUID, user: User): Optional<BmcModificationRequest>
}