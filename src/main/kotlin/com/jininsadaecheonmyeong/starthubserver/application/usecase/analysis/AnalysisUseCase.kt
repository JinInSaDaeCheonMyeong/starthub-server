package com.jininsadaecheonmyeong.starthubserver.application.usecase.analysis

import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.analysis.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.analysis.CompetitorAnalysisResponse

interface AnalysisUseCase {
    suspend fun analyzeCompetitors(request: CompetitorAnalysisRequest): CompetitorAnalysisResponse

    suspend fun regenerateAnalysis(bmcId: Long): CompetitorAnalysisResponse

    suspend fun performAnalysisInternal(
        user: User,
        userBmc: BusinessModelCanvas,
    ): CompetitorAnalysisResponse

    fun getAnalysisByBmcId(bmcId: Long): CompetitorAnalysisResponse

    fun getAllAnalysesByUser(): List<CompetitorAnalysisResponse>

    fun performAnalysisAsync(
        user: User,
        businessModelCanvas: BusinessModelCanvas,
    )
}
