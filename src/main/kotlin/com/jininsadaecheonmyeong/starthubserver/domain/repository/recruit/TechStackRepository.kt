package com.jininsadaecheonmyeong.starthubserver.domain.repository.recruit

import com.jininsadaecheonmyeong.starthubserver.domain.entity.recruit.TechStack
import org.springframework.data.jpa.repository.JpaRepository

interface TechStackRepository : JpaRepository<TechStack, Long> {
    fun findByName(name: String): TechStack?
}
