package com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AnnouncementRepository : JpaRepository<Announcement, Long> {
    fun existsByUrl(url: String): Boolean

    fun findAllByStatus(
        status: AnnouncementStatus,
        pageable: Pageable,
    ): Page<Announcement>

    fun findAllByStatus(status: AnnouncementStatus): List<Announcement>

    @Query("""
        SELECT a FROM Announcement a 
        WHERE a.status = 'ACTIVE'
        AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:supportField IS NULL OR a.supportField = :supportField)
        AND (:region IS NULL OR a.region = :region)
        AND (:targetAge IS NULL OR a.targetAge = :targetAge)
        AND (:startupHistory IS NULL OR a.startupHistory = :startupHistory)
    """)
    fun searchAnnouncements(
        @Param("title") title: String?,
        @Param("supportField") supportField: String?,
        @Param("region") region: String?,
        @Param("targetAge") targetAge: String?,
        @Param("startupHistory") startupHistory: String?,
        pageable: Pageable
    ): Page<Announcement>
}
