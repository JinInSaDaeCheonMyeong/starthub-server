package com.jininsadaecheonmyeong.starthubserver.domain.analysis.repository

import com.jininsadaecheonmyeong.starthubserver.domain.analysis.entity.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CompetitorAnalysisRepository : JpaRepository<CompetitorAnalysis, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<CompetitorAnalysis>

    fun findAllByUserAndDeletedFalse(user: User): List<CompetitorAnalysis>

    fun findByBusinessModelCanvasAndDeletedFalse(businessModelCanvas: BusinessModelCanvas): Optional<CompetitorAnalysis>
}
