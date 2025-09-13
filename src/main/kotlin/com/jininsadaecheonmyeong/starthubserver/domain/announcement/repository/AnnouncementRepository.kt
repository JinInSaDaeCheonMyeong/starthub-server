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

    @Query(
        """
        SELECT a FROM Announcement a
        WHERE a.status = 'ACTIVE'
        AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:supportField IS NULL OR LOWER(a.supportField) LIKE LOWER(CONCAT('%', :supportField, '%')))
        AND (:targetRegion IS NULL OR LOWER(a.region) LIKE LOWER(CONCAT('%', :targetRegion, '%')))
        AND (:targetGroup IS NULL OR LOWER(a.organizationType) LIKE LOWER(CONCAT('%', :targetGroup, '%')))
        AND (:targetAge IS NULL OR LOWER(a.targetAge) LIKE LOWER(CONCAT('%', :targetAge, '%')))
        AND (:businessExperience IS NULL OR LOWER(a.startupHistory) LIKE LOWER(CONCAT('%', :businessExperience, '%')))
        """,
    )
    fun searchAnnouncements(
        @Param("title") title: String?,
        @Param("supportField") supportField: String?,
        @Param("targetRegion") targetRegion: String?,
        @Param("targetGroup") targetGroup: String?,
        @Param("targetAge") targetAge: String?,
        @Param("businessExperience") businessExperience: String?,
        pageable: Pageable,
    ): Page<Announcement>
}
