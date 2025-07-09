package com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BusinessModelCanvasRepository : JpaRepository<BusinessModelCanvas, UUID> {
    fun findByIdAndDeletedFalse(id: UUID): Optional<BusinessModelCanvas>
    fun findAllByUserAndDeletedFalse(user: User): List<BusinessModelCanvas>
    fun findAllByDeletedFalse(): List<BusinessModelCanvas>
    fun existsByIdAndDeletedFalse(id: UUID): Boolean
}