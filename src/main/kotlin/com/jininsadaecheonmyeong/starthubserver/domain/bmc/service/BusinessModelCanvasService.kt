package com.jininsadaecheonmyeong.starthubserver.domain.bmc.service

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.data.response.BusinessModelCanvasResponse
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.exception.BusinessModelCanvasNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.bmc.repository.BusinessModelCanvasRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.support.UserAuthenticationHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class BusinessModelCanvasService(
    private val businessModelCanvasRepository: BusinessModelCanvasRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder
) {

    @Transactional(readOnly = true)
    fun getBusinessModelCanvas(id: UUID): BusinessModelCanvasResponse {
        val user = userAuthenticationHolder.current()
        val bmc = businessModelCanvasRepository.findByIdAndDeletedFalse(id)
            .orElseThrow { BusinessModelCanvasNotFoundException("Business Model Canvas를 찾을 수 없습니다.") }
        
        if (!bmc.isOwner(user)) {
            throw BusinessModelCanvasNotFoundException("접근 권한이 없습니다.")
        }
        
        return BusinessModelCanvasResponse.from(bmc)
    }

    @Transactional(readOnly = true)
    fun getAllBusinessModelCanvases(): List<BusinessModelCanvasResponse> {
        val user = userAuthenticationHolder.current()
        val bmcs = businessModelCanvasRepository.findAllByUserAndDeletedFalse(user)
        
        return bmcs.map { BusinessModelCanvasResponse.from(it) }
    }

    fun deleteBusinessModelCanvas(id: UUID) {
        val user = userAuthenticationHolder.current()
        val bmc = businessModelCanvasRepository.findByIdAndDeletedFalse(id)
            .orElseThrow { BusinessModelCanvasNotFoundException("Business Model Canvas를 찾을 수 없습니다.") }
        
        if (!bmc.isOwner(user)) {
            throw BusinessModelCanvasNotFoundException("접근 권한이 없습니다.")
        }
        
        bmc.delete()
        businessModelCanvasRepository.save(bmc)
    }
}