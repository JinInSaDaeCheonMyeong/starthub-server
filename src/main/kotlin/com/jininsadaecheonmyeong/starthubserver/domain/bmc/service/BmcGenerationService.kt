package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.GenerateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BmcSessionNotCompletedException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.ai.chat.model.ChatModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BmcGenerationService(
    private val chatModel: ChatModel,
    private val bmcQuestionService: BmcQuestionService,
    private val bmcPromptService: BmcPromptService,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
) {
    private val log = logger()

    fun generateBusinessModelCanvas(request: GenerateBmcRequest): BusinessModelCanvasResponse {
        val user = UserAuthenticationHolder.current()
        val bmcQuestion = bmcQuestionService.getBmcQuestionEntity(request.sessionId)

        if (!bmcQuestion.isCompleted) {
            throw BmcSessionNotCompletedException("모든 질문에 답변을 완료해야 BMC를 생성할 수 있습니다.")
        }

        val prompt = bmcPromptService.generateBmcPrompt(bmcQuestion)

        try {
            val response = chatModel.call(prompt)

            val bmcElements = parseBmcResponse(response)

            val businessModelCanvas =
                BusinessModelCanvas(
                    user = user,
                    title = request.title,
                    keyPartners = bmcElements["KEY_PARTNERS"],
                    keyActivities = bmcElements["KEY_ACTIVITIES"],
                    keyResources = bmcElements["KEY_RESOURCES"],
                    valueProposition = bmcElements["VALUE_PROPOSITION"],
                    customerRelationships = bmcElements["CUSTOMER_RELATIONSHIPS"],
                    channels = bmcElements["CHANNELS"],
                    customerSegments = bmcElements["CUSTOMER_SEGMENTS"],
                    costStructure = bmcElements["COST_STRUCTURE"],
                    revenueStreams = bmcElements["REVENUE_STREAMS"],
                    isCompleted = true,
                    bmcQuestion = bmcQuestion,
                )

            val savedBmc = businessModelCanvasRepository.save(businessModelCanvas)
            log.info("BMC 생성 완료: sessionId={}, userId={}, bmcId={}", request.sessionId, user.id, savedBmc.id)

            return BusinessModelCanvasResponse.from(savedBmc)
        } catch (e: Exception) {
            log.error("BMC 생성 중 오류 발생: sessionId={}, userId={}, error={}", request.sessionId, user.id, e.message, e)
            throw RuntimeException("BMC 생성 중 오류가 발생했습니다. 다시 시도해주세요.", e)
        }
    }

    private fun parseBmcResponse(response: String): Map<String, String> {
        val bmcElements = mutableMapOf<String, String>()
        val lines = response.lines()

        val keys =
            listOf(
                "KEY_PARTNERS",
                "KEY_ACTIVITIES",
                "KEY_RESOURCES",
                "VALUE_PROPOSITION",
                "CUSTOMER_RELATIONSHIPS",
                "CHANNELS",
                "CUSTOMER_SEGMENTS",
                "COST_STRUCTURE",
                "REVENUE_STREAMS",
            )

        keys.forEach { key ->
            val content = extractContent(lines, key)
            if (content.isNotBlank()) {
                bmcElements[key] = content
            }
        }

        return bmcElements
    }

    private fun extractContent(
        lines: List<String>,
        key: String,
    ): String {
        val startIndex = lines.indexOfFirst { it.startsWith("$key:") }
        if (startIndex == -1) return ""

        val content = StringBuilder()
        var currentIndex = startIndex

        val firstLine = lines[currentIndex].removePrefix("$key:").trim()
        if (firstLine.isNotEmpty()) {
            content.append(firstLine)
        }

        currentIndex++
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            if (line.isEmpty()) {
                currentIndex++
                continue
            }

            if (line.contains(":") && line.split(":")[0].trim() in
                listOf(
                    "KEY_PARTNERS", "KEY_ACTIVITIES", "KEY_RESOURCES", "VALUE_PROPOSITION",
                    "CUSTOMER_RELATIONSHIPS", "CHANNELS", "CUSTOMER_SEGMENTS", "COST_STRUCTURE", "REVENUE_STREAMS",
                )
            ) {
                break
            }

            if (content.isNotEmpty()) {
                content.append("\n")
            }
            content.append(line)
            currentIndex++
        }

        return content.toString().trim()
    }
}
