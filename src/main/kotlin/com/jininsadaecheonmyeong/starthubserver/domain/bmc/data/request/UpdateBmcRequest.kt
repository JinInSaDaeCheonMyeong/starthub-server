package com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.enums.BmcTemplateType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UpdateBmcRequest(
    @field:NotNull(message = "BMC ID를 입력해주세요.")
    val bmcId: Long,
    @field:Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
    val title: String? = null,
    val templateType: BmcTemplateType? = null,
    @field:Size(max = 2000, message = "목표 고객은 2000자 이하로 입력해주세요.")
    val customerSegments: String? = null,
    @field:Size(max = 2000, message = "제공 가치는 2000자 이하로 입력해주세요.")
    val valueProposition: String? = null,
    @field:Size(max = 2000, message = "채널은 2000자 이하로 입력해주세요.")
    val channels: String? = null,
    @field:Size(max = 2000, message = "고객 관계는 2000자 이하로 입력해주세요.")
    val customerRelationships: String? = null,
    @field:Size(max = 2000, message = "수익 구조는 2000자 이하로 입력해주세요.")
    val revenueStreams: String? = null,
    @field:Size(max = 2000, message = "핵심 자원은 2000자 이하로 입력해주세요.")
    val keyResources: String? = null,
    @field:Size(max = 2000, message = "핵심 활동은 2000자 이하로 입력해주세요.")
    val keyActivities: String? = null,
    @field:Size(max = 2000, message = "핵심 파트너는 2000자 이하로 입력해주세요.")
    val keyPartners: String? = null,
    @field:Size(max = 2000, message = "비용 구조는 2000자 이하로 입력해주세요.")
    val costStructure: String? = null,
    val imageUrl: String? = null,
)
