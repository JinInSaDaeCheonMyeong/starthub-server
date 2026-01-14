package com.jininsadaecheonmyeong.starthubserver.usecase.analysis

import com.jininsadaecheonmyeong.starthubserver.dto.request.analysis.CompetitorAnalysisRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.analysis.CompetitorAnalysisResponse
import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User

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
