package com.jininsadaecheonmyeong.starthubserver.domain.recruit.repository

import com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity.TechStack
import org.springframework.data.jpa.repository.JpaRepository

interface TechStackRepository : JpaRepository<TechStack, Long> {
    fun findByName(name: String): TechStack?
}
