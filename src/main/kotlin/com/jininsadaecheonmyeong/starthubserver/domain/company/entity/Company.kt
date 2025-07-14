package com.jininsadaecheonmyeong.starthubserver.domain.company.entity

import com.jininsadaecheonmyeong.starthubserver.domain.company.data.request.UpdateCompanyProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.response.CompanyListResponse
import com.jininsadaecheonmyeong.starthubserver.domain.company.data.response.CompanyResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "companies")
class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val companyName: String,
    @Column(nullable = false)
    var companyDescription: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val companyCategory: BusinessType,
    @Column(nullable = false)
    var businessDescription: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_founder_id", nullable = false)
    val founder: User,
    var companyUrl: String? = null,
    @Column(nullable = false)
    var contactEmail: String,
    @Column(nullable = false)
    var contactNumber: String,
    var address: String? = null,
    @Column(nullable = false)
    var employeeCount: Int,
    var logoImage: String? = null,
    @Column(nullable = false)
    var deleted: Boolean = false,
) : BaseEntity() {
    fun isFounder(user: User): Boolean = founder.id == user.id

    fun updateProfile(request: UpdateCompanyProfileRequest) {
        request.companyDescription?.let { this.companyDescription = it }
        request.businessDescription?.let { this.businessDescription = it }
        request.pageUrl?.let { this.companyUrl = it }
        request.email?.let { this.contactEmail = it }
        request.tel?.let { this.contactNumber = it }
        request.address?.let { this.address = it }
        request.employeeCount?.let { this.employeeCount = it }
        request.logoImage?.let { this.logoImage = it }
    }

    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted

    fun toCompanyResponse(): CompanyResponse {
        return CompanyResponse(
            id = this.id!!,
            companyName = this.companyName,
            companyDescription = this.companyDescription,
            companyCategory = this.companyCategory,
            businessDescription = this.businessDescription,
            founderId = this.founder.id!!,
            founderName = this.founder.username,
            companyUrl = this.companyUrl,
            contactEmail = this.contactEmail,
            contactNumber = this.contactNumber,
            address = this.address,
            employeeCount = this.employeeCount,
            logoImage = this.logoImage,
            createdAt = this.createdAt,
        )
    }

    fun toCompanyListResponse(): CompanyListResponse {
        return CompanyListResponse(
            id = this.id!!,
            companyName = this.companyName,
            companyDescription = this.companyDescription,
            companyCategory = this.companyCategory,
            logoImage = this.logoImage,
        )
    }
}
