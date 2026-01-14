package com.jininsadaecheonmyeong.starthubserver.repository.recruit

import com.jininsadaecheonmyeong.starthubserver.entity.recruit.Recruit
import com.jininsadaecheonmyeong.starthubserver.entity.recruit.RecruitTechStack
import org.springframework.data.jpa.repository.JpaRepository

interface RecruitTechStackRepository : JpaRepository<RecruitTechStack, Long> {
    fun findByRecruit(recruit: Recruit): List<RecruitTechStack>
}
