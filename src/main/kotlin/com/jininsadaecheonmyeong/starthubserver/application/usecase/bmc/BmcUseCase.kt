package com.jininsadaecheonmyeong.starthubserver.application.usecase.bmc

import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.AnswerQuestionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.CreateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.UpdateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.bmc.UpdateBmcSessionRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc.BmcFormResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc.BmcSessionResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.bmc.BusinessModelCanvasResponse
import org.springframework.web.multipart.MultipartFile

interface BmcUseCase {
    fun createBmcSession(request: CreateBmcSessionRequest): Pair<BmcSessionResponse, String>

    fun answerQuestion(request: AnswerQuestionRequest): BmcSessionResponse

    fun getBmcSession(sessionId: Long): BmcSessionResponse

    fun getAllBmcSessions(): List<BmcSessionResponse>

    fun getBmcQuestions(): List<BmcFormResponse>

    fun updateSessionAnswers(
        sessionId: Long,
        request: UpdateBmcSessionRequest,
    ): BmcSessionResponse

    fun generateBusinessModelCanvas(request: GenerateBmcRequest): Pair<BusinessModelCanvasResponse, String>

    fun updateSessionAnswersAndRegenerate(
        sessionId: Long,
        request: UpdateBmcSessionRequest,
    ): BusinessModelCanvasResponse

    fun requestBmcModification(request: ModifyBmcRequest): BmcModificationResponse

    fun getBmcModificationHistory(bmcId: Long): List<BmcModificationResponse>

    fun getBusinessModelCanvas(id: Long): BusinessModelCanvasResponse

    fun getAllBusinessModelCanvases(): List<BusinessModelCanvasResponse>

    fun deleteBusinessModelCanvas(id: Long)

    fun updateBusinessModelCanvas(request: UpdateBmcRequest): BusinessModelCanvasResponse

    fun uploadBmcImage(
        bmcId: Long,
        imageFile: MultipartFile,
    ): BusinessModelCanvasResponse

    fun getAnonymousCompletedBmcs(limit: Int = 50): List<AnonymousBmcData>

    fun findSimilarBmcs(
        targetBmc: AnonymousBmcData,
        limit: Int = 10,
    ): List<AnonymousBmcData>
}

data class AnonymousBmcData(
    val valueProposition: String?,
    val customerSegments: String?,
    val channels: String?,
    val customerRelationships: String?,
    val revenueStreams: String?,
    val keyResources: String?,
    val keyActivities: String?,
    val keyPartners: String?,
    val costStructure: String?,
)
