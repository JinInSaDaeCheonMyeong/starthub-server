package com.jininsadaecheonmyeong.starthubserver.domain.company.repository

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CompanyRepository : JpaRepository<Company, UUID> {

    fun findAllByDeletedFalse(): List<Company>

    fun findByIdAndDeletedFalse(id: UUID): Optional<Company?>

    fun findByCompanyNameAndDeletedFalse(name: String): Optional<Company?>

    fun existsByCompanyNameAndDeletedFalse(name: String): Boolean

    fun findByCompanyCategoryAndDeletedFalse(category: BusinessType): List<Company>

    fun findByFounderAndDeletedFalse(user: User): List<Company>

}