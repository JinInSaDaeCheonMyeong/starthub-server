package com.jininsadaecheonmyeong.starthubserver.domain.repository.analysis

import com.jininsadaecheonmyeong.starthubserver.domain.entity.analysis.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CompetitorAnalysisRepository : JpaRepository<CompetitorAnalysis, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<CompetitorAnalysis>

    fun findAllByUserAndDeletedFalse(user: User): List<CompetitorAnalysis>

    fun findByBusinessModelCanvasAndDeletedFalse(businessModelCanvas: BusinessModelCanvas): Optional<CompetitorAnalysis>
}
