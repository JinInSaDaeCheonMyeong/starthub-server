package com.jininsadaecheonmyeong.starthubserver.repository.recruit

import com.jininsadaecheonmyeong.starthubserver.entity.recruit.TechStack
import org.springframework.data.jpa.repository.JpaRepository

interface TechStackRepository : JpaRepository<TechStack, Long> {
    fun findByName(name: String): TechStack?
}
