package com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitPosition
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.enums.RecruitRole

data class RecruitResponse(
    val id: Long,
    val role: RecruitRole,
    val count: Int,
    val description: String,
    val closed: Boolean
) {
    companion object {
        fun from(entity: RecruitPosition) = RecruitResponse(
            id = entity.id!!,
            role = entity.role,
            count = entity.count,
            description = entity.description,
            closed = entity.closed
        )
    }
}