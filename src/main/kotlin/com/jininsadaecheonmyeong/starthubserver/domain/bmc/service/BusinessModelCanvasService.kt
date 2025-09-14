package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.request.UpdateBmcRequest
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BmcNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BusinessModelCanvasService(
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
) {
    @Transactional(readOnly = true)
    fun getBusinessModelCanvas(id: Long): BusinessModelCanvasResponse {
        val user = UserAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(id)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        return BusinessModelCanvasResponse.from(bmc)
    }

    @Transactional(readOnly = true)
    fun getAllBusinessModelCanvases(): List<BusinessModelCanvasResponse> {
        val user = UserAuthenticationHolder.current()
        val bmcs = businessModelCanvasRepository.findAllByUserAndDeletedFalse(user)

        return bmcs.map { BusinessModelCanvasResponse.from(it) }
    }

    fun deleteBusinessModelCanvas(id: Long) {
        val user = UserAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(id)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        bmc.delete()
        businessModelCanvasRepository.save(bmc)
    }

    fun updateBusinessModelCanvas(request: UpdateBmcRequest): BusinessModelCanvasResponse {
        val user = UserAuthenticationHolder.current()
        val bmc =
            businessModelCanvasRepository.findByIdAndDeletedFalse(request.bmcId)
                .orElseThrow { BmcNotFoundException("BMC를 찾을 수 없습니다.") }

        if (!bmc.isOwner(user)) throw BmcNotFoundException("접근 권한이 없습니다.")

        bmc.updateCanvas(
            title = request.title,
            customerSegments = request.customerSegments,
            valueProposition = request.valueProposition,
            channels = request.channels,
            customerRelationships = request.customerRelationships,
            revenueStreams = request.revenueStreams,
            keyResources = request.keyResources,
            keyActivities = request.keyActivities,
            keyPartners = request.keyPartners,
            costStructure = request.costStructure,
        )

        val updatedBmc = businessModelCanvasRepository.save(bmc)
        return BusinessModelCanvasResponse.from(updatedBmc)
    }
}
