package com.jininsadaecheonmyeong.starthubserver.domain.repository.recruit

import com.jininsadaecheonmyeong.starthubserver.domain.entity.recruit.Recruit
import com.jininsadaecheonmyeong.starthubserver.domain.entity.recruit.RecruitTechStack
import org.springframework.data.jpa.repository.JpaRepository

interface RecruitTechStackRepository : JpaRepository<RecruitTechStack, Long> {
    fun findByRecruit(recruit: Recruit): List<RecruitTechStack>
}
