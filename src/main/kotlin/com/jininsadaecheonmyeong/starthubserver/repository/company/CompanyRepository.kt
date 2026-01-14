package com.jininsadaecheonmyeong.starthubserver.repository.company

import com.jininsadaecheonmyeong.starthubserver.entity.company.Company
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.enums.user.BusinessType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CompanyRepository : JpaRepository<Company, Long> {
    fun findAllByDeletedFalse(): List<Company>

    fun existsByCompanyNameAndDeletedFalse(name: String): Boolean

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.id = :id AND c.deleted = false",
    )
    fun findByIdAndDeletedFalse(id: Long): Company?

    @Query(
        "SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.companyName = :name AND c.deleted = false",
    )
    fun findByCompanyNameAndDeletedFalse(name: String): Company?

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
