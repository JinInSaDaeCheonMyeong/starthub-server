package com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitPosition
import org.springframework.data.jpa.repository.JpaRepository

interface RecruitPositionRepository : JpaRepository<RecruitPosition, Long> {
    fun findAllByCompanyAndClosedFalse(company: Company): List<RecruitPosition>
}