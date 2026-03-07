package com.jininsadaecheonmyeong.starthubserver.domain.repository.analysis

import com.jininsadaecheonmyeong.starthubserver.domain.entity.analysis.CompetitorAnalysis
import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface CompetitorAnalysisRepository : JpaRepository<CompetitorAnalysis, Long> {
    fun findByIdAndDeletedFalse(id: Long): Optional<CompetitorAnalysis>

    @Query("SELECT ca FROM CompetitorAnalysis ca JOIN FETCH ca.businessModelCanvas WHERE ca.user = :user AND ca.deleted = false")
    fun findAllByUserAndDeletedFalse(user: User): List<CompetitorAnalysis>

    fun findByBusinessModelCanvasAndDeletedFalse(businessModelCanvas: BusinessModelCanvas): Optional<CompetitorAnalysis>
}
