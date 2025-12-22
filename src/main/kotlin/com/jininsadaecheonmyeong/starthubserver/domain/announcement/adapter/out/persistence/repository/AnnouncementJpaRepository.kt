package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.repository

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.entity.AnnouncementJpaEntity
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AnnouncementJpaRepository : JpaRepository<AnnouncementJpaEntity, Long> {
    fun existsByUrl(url: String): Boolean

    fun findAllByTitleIn(titles: List<String>): List<AnnouncementJpaEntity>

    fun findAllByStatus(
        status: AnnouncementStatus,
        pageable: Pageable,
    ): Page<AnnouncementJpaEntity>

    fun findAllByStatus(status: AnnouncementStatus): List<AnnouncementJpaEntity>

    @Query(
        """
        SELECT a FROM AnnouncementJpaEntity a
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
    ): Page<AnnouncementJpaEntity>
}
