package com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.Recruit
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.RecruitTechStack
import org.springframework.data.jpa.repository.JpaRepository

interface RecruitTechStackRepository : JpaRepository<RecruitTechStack, Long> {
    fun findByRecruit(recruit: Recruit): List<RecruitTechStack>
}
