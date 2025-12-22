package com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.repository

import com.jininsadaecheonmyeong.starthubserver.domain.company.adapter.out.persistence.entity.CompanyJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Company JPA Repository
 * - Persistence Adapter에서 사용
 */
interface CompanyJpaRepository : JpaRepository<CompanyJpaEntity, Long> {
    fun findAllByDeletedFalse(): List<CompanyJpaEntity>

    fun existsByCompanyNameAndDeletedFalse(name: String): Boolean

    @Query(
        "SELECT c FROM CompanyJpaEntity c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.id = :id AND c.deleted = false"
    )
    fun findByIdAndDeletedFalse(id: Long): CompanyJpaEntity?

    @Query(
        "SELECT c FROM CompanyJpaEntity c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.companyName = :name AND c.deleted = false"
    )
    fun findByCompanyNameAndDeletedFalse(name: String): CompanyJpaEntity?

    @Query(
        "SELECT c FROM CompanyJpaEntity c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.companyCategory = :category AND c.deleted = false"
    )
    fun findByCompanyCategoryAndDeletedFalse(category: BusinessType): List<CompanyJpaEntity>

    @Query(
        "SELECT c FROM CompanyJpaEntity c " +
            "LEFT JOIN FETCH c.founder " +
            "WHERE c.founder = :user AND c.deleted = false"
    )
    fun findByFounderAndDeletedFalse(user: User): List<CompanyJpaEntity>
}
