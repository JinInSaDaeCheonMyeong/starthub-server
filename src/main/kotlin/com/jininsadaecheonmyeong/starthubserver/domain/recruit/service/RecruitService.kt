package com.jininsadaecheonmyeong.starthubserver.domain.recruit.service

import com.jininsadaecheonmyeong.starthubserver.domain.company.exception.CompanyNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.company.repository.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.request.CreateRecruitRequest
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitPosition
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository.RecruitPositionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RecruitService(
    private val recruitRepo: RecruitPositionRepository,
    private val companyRepo: CompanyRepository
) {
    @Transactional
    fun create(companyId: UUID, request: CreateRecruitRequest): RecruitResponse {
        val company = companyRepo.findById(companyId)
            .orElseThrow { CompanyNotFoundException("존재하지 않는 기업입니다.") }

        val position = RecruitPosition(
            company = company,
            role = request.role,
            count = request.count,
            description = request.description
        )
        return RecruitResponse.from(recruitRepo.save(position))
    }

    @Transactional(readOnly = true)
    fun getByCompany(companyId: UUID): List<RecruitResponse> {
        val company = companyRepo.findById(companyId)
            .orElseThrow { CompanyNotFoundException("존재하지 않는 기업입니다.") }
        return recruitRepo.findAllByCompanyAndClosedFalse(company)
            .map { RecruitResponse.from(it) }
    }
}