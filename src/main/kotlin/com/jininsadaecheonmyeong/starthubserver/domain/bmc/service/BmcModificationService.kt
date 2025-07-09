package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.ModifyBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BmcModificationResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcModificationRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BmcModificationType
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BusinessModelCanvasNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BmcModificationRequestRepository
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.logger
import org.springframework.ai.chat.model.ChatModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class BmcModificationService(
    private val chatModel: ChatModel,
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val bmcModificationRequestRepository: BmcModificationRequestRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder
) {
    private val log = logger()

    fun requestBmcModification(request: ModifyBmcRequest): BmcModificationResponse {
        val user = userAuthenticationHolder.current()
        val bmc = businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
            .orElseThrow { BusinessModelCanvasNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) {
            throw BusinessModelCanvasNotFoundException("접근 권한이 없습니다.")
        }

        val modificationRequest = BmcModificationRequest(
            user = user,
            businessModelCanvas = bmc,
            modificationRequest = request.modificationRequest,
            requestType = request.requestType
        )

        val savedRequest = bmcModificationRequestRepository.save(modificationRequest)

        try {
            val prompt = when (request.requestType) {
                BmcModificationType.MODIFY -> generateModificationPrompt(bmc, request.modificationRequest)
                BmcModificationType.REGENERATE -> generateRegenerationPrompt(bmc, request.modificationRequest)
            }

            val aiResponse = chatModel.call(prompt)
            val bmcElements = parseBmcResponse(aiResponse)

            bmc.updateCanvas(
                keyPartners = bmcElements["KEY_PARTNERS"],
                keyActivities = bmcElements["KEY_ACTIVITIES"],
                keyResources = bmcElements["KEY_RESOURCES"],
                valueProposition = bmcElements["VALUE_PROPOSITION"],
                customerRelationships = bmcElements["CUSTOMER_RELATIONSHIPS"],
                channels = bmcElements["CHANNELS"],
                customerSegments = bmcElements["CUSTOMER_SEGMENTS"],
                costStructure = bmcElements["COST_STRUCTURE"],
                revenueStreams = bmcElements["REVENUE_STREAMS"]
            )

            val updatedBmc = businessModelCanvasRepository.save(bmc)
            savedRequest.markAsProcessed(aiResponse)
            bmcModificationRequestRepository.save(savedRequest)

            log.info("BMC 수정 완료: bmcId={}, userId={}, requestType={}", request.bmcId, user.id, request.requestType)

            return BmcModificationResponse.from(savedRequest, BusinessModelCanvasResponse.from(updatedBmc))

        } catch (e: Exception) {
            log.error("BMC 수정 중 오류 발생: bmcId={}, userId={}, error={}", request.bmcId, user.id, e.message, e)
            throw RuntimeException("BMC 수정 중 오류가 발생했습니다. 다시 시도해주세요.", e)
        }
    }

    @Transactional(readOnly = true)
    fun getBmcModificationHistory(bmcId: UUID): List<BmcModificationResponse> {
        val user = userAuthenticationHolder.current()
        val bmc = businessModelCanvasRepository.findByIdAndDeletedFalse(bmcId)
            .orElseThrow { BusinessModelCanvasNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) {
            throw BusinessModelCanvasNotFoundException("접근 권한이 없습니다.")
        }

        val modifications = bmcModificationRequestRepository.findByBusinessModelCanvasAndUserOrderByCreatedAtDesc(bmc, user)
        return modifications.map { BmcModificationResponse.from(it) }
    }

    private fun generateModificationPrompt(bmc: com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas, modificationRequest: String): String {
        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("기존 BMC를 사용자의 요청에 따라 수정해주세요.")
            appendLine()
            appendLine("현재 BMC:")
            appendLine("제목: ${bmc.title}")
            appendLine("1. 핵심 파트너: ${bmc.keyPartners ?: "없음"}")
            appendLine("2. 핵심 활동: ${bmc.keyActivities ?: "없음"}")
            appendLine("3. 핵심 자원: ${bmc.keyResources ?: "없음"}")
            appendLine("4. 제공 가치: ${bmc.valueProposition ?: "없음"}")
            appendLine("5. 고객 관계: ${bmc.customerRelationships ?: "없음"}")
            appendLine("6. 채널: ${bmc.channels ?: "없음"}")
            appendLine("7. 목표 고객: ${bmc.customerSegments ?: "없음"}")
            appendLine("8. 비용 구조: ${bmc.costStructure ?: "없음"}")
            appendLine("9. 수익 구조: ${bmc.revenueStreams ?: "없음"}")
            appendLine()
            appendLine("수정 요청:")
            appendLine(modificationRequest)
            appendLine()
            appendLine("위 수정 요청을 반영하여 BMC를 업데이트해주세요.")
            appendLine("수정이 필요하지 않은 부분은 기존 내용을 그대로 유지해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
        }
    }

    private fun generateRegenerationPrompt(bmc: com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas, additionalContext: String): String {
        return buildString {
            appendLine("당신은 비즈니스 모델 캔버스(BMC) 전문가입니다.")
            appendLine("기존 BMC를 참고하여 완전히 새로운 관점에서 BMC를 재생성해주세요.")
            appendLine()
            appendLine("기존 BMC (참고용):")
            appendLine("제목: ${bmc.title}")
            appendLine("1. 핵심 파트너: ${bmc.keyPartners ?: "없음"}")
            appendLine("2. 핵심 활동: ${bmc.keyActivities ?: "없음"}")
            appendLine("3. 핵심 자원: ${bmc.keyResources ?: "없음"}")
            appendLine("4. 제공 가치: ${bmc.valueProposition ?: "없음"}")
            appendLine("5. 고객 관계: ${bmc.customerRelationships ?: "없음"}")
            appendLine("6. 채널: ${bmc.channels ?: "없음"}")
            appendLine("7. 목표 고객: ${bmc.customerSegments ?: "없음"}")
            appendLine("8. 비용 구조: ${bmc.costStructure ?: "없음"}")
            appendLine("9. 수익 구조: ${bmc.revenueStreams ?: "없음"}")
            appendLine()
            appendLine("추가 고려사항:")
            appendLine(additionalContext)
            appendLine()
            appendLine("위 정보를 참고하여 더 구체적이고 실현 가능한 BMC를 새롭게 생성해주세요.")
            appendLine("기존 아이디어는 유지하되, 더 나은 접근 방식과 전략을 제시해주세요.")
            appendLine()
            appendLine("응답 형식:")
            appendLine("KEY_PARTNERS: (내용)")
            appendLine("KEY_ACTIVITIES: (내용)")
            appendLine("KEY_RESOURCES: (내용)")
            appendLine("VALUE_PROPOSITION: (내용)")
            appendLine("CUSTOMER_RELATIONSHIPS: (내용)")
            appendLine("CHANNELS: (내용)")
            appendLine("CUSTOMER_SEGMENTS: (내용)")
            appendLine("COST_STRUCTURE: (내용)")
            appendLine("REVENUE_STREAMS: (내용)")
        }
    }

    private fun parseBmcResponse(response: String): Map<String, String> {
        val bmcElements = mutableMapOf<String, String>()
        val lines = response.lines()
        
        val keys = listOf(
            "KEY_PARTNERS",
            "KEY_ACTIVITIES", 
            "KEY_RESOURCES",
            "VALUE_PROPOSITION",
            "CUSTOMER_RELATIONSHIPS",
            "CHANNELS",
            "CUSTOMER_SEGMENTS",
            "COST_STRUCTURE",
            "REVENUE_STREAMS"
        )
        
        keys.forEach { key ->
            val content = extractContent(lines, key)
            if (content.isNotBlank()) {
                bmcElements[key] = content
            }
        }
        
        return bmcElements
    }

    private fun extractContent(lines: List<String>, key: String): String {
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
            
            if (line.contains(":") && line.split(":")[0].trim() in listOf(
                "KEY_PARTNERS", "KEY_ACTIVITIES", "KEY_RESOURCES", "VALUE_PROPOSITION",
                "CUSTOMER_RELATIONSHIPS", "CHANNELS", "CUSTOMER_SEGMENTS", "COST_STRUCTURE", "REVENUE_STREAMS"
            )) {
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