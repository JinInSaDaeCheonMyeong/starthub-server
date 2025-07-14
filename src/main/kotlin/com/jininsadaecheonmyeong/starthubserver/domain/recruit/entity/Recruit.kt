
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.entity

import com.jininsadaecheonmyeong.starthubserver.domain.company.entity.Company
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitResponse
import com.jininsadaecheonmyeong.starthubserver.domain.recruit.data.response.RecruitSummaryResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "recruits")
class Recruit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    val writer: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,
    @Column(nullable = false)
    var startDate: String,
    @Column(nullable = false)
    var endDate: String,
    @Column(nullable = false)
    var desiredCareer: String,
    @Column(nullable = false)
    var workType: String,
    @Column(nullable = false)
    var jobType: String,
    @Column(nullable = false)
    var requiredPeople: Int,
    @Column(nullable = false)
    var viewCount: Int = 0,
    @Column(nullable = false)
    var isClosed: Boolean = false,
    @Column(nullable = false)
    var deleted: Boolean = false,
) : BaseEntity() {
    fun delete() {
        this.deleted = true
    }

    fun isDeleted(): Boolean = deleted

    fun close() {
        this.isClosed = true
    }

    fun isClosed(): Boolean = isClosed

    fun toResponse(techStack: List<String>): RecruitResponse {
        return RecruitResponse(
            id = this.id!!,
            title = this.title,
            content = this.content,
            writerId = this.writer.id!!,
            writerNickname = this.writer.username ?: "",
            companyId = this.company.id!!,
            companyName = this.company.companyName,
            startDate = this.startDate,
            endDate = this.endDate,
            desiredCareer = this.desiredCareer,
            workType = this.workType,
            jobType = this.jobType,
            requiredPeople = this.requiredPeople,
            viewCount = this.viewCount,
            isClosed = this.isClosed,
            techStack = techStack,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }

    fun toSummaryResponse(): RecruitSummaryResponse {
        return RecruitSummaryResponse(
            id = this.id!!,
            title = this.title,
            companyName = this.company.companyName,
            endDate = this.endDate,
            viewCount = this.viewCount,
            isClosed = this.isClosed,
            createdAt = this.createdAt,
        )
    }
}
