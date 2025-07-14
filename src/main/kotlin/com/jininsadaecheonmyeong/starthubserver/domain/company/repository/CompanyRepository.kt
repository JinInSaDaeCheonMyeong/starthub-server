package com.jininsadaecheonmyeong.starthubserver.domain.company.repository

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface CompanyRepository : JpaRepository<Company, Long> {
    fun findAllByDeletedFalse(): List<Company>

    fun existsByCompanyNameAndDeletedFalse(name: String): Boolean

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.id = :id AND c.deleted = false",
    )
    fun findByIdAndDeletedFalse(id: Long): Optional<Company>

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.companyName = :name AND c.deleted = false",
    )
    fun findByCompanyNameAndDeletedFalse(name: String): Optional<Company>

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.companyCategory = :category AND c.deleted = false",
    )
    fun findByCompanyCategoryAndDeletedFalse(category: BusinessType): List<Company>

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.founder = :user AND c.deleted = false",
    )
    fun findByFounderAndDeletedFalse(user: User): List<Company>
}
