package com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.mapper.AnnouncementLikePersistenceMapper
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.mapper.AnnouncementPersistenceMapper
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.repository.AnnouncementJpaRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.adapter.out.persistence.repository.AnnouncementLikeJpaRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.DeleteAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.LoadAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.SaveAnnouncementLikePort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.application.port.out.SaveAnnouncementPort
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.Announcement
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.domain.model.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class AnnouncementPersistenceAdapter(
    private val announcementJpaRepository: AnnouncementJpaRepository,
    private val announcementLikeJpaRepository: AnnouncementLikeJpaRepository,
    private val announcementMapper: AnnouncementPersistenceMapper,
    private val announcementLikeMapper: AnnouncementLikePersistenceMapper,
    private val userRepository: UserRepository
) : LoadAnnouncementPort, SaveAnnouncementPort,
    LoadAnnouncementLikePort, SaveAnnouncementLikePort, DeleteAnnouncementLikePort {

    // LoadAnnouncementPort implementations
    override fun loadById(id: Long): Announcement? {
        return announcementJpaRepository.findByIdOrNull(id)
            ?.let { announcementMapper.toDomain(it) }
    }

    override fun loadAllByStatus(status: AnnouncementStatus, pageable: Pageable): Page<Announcement> {
        return announcementJpaRepository.findAllByStatus(status, pageable)
            .map { announcementMapper.toDomain(it) }
    }

    override fun loadAllByStatus(status: AnnouncementStatus): List<Announcement> {
        return announcementJpaRepository.findAllByStatus(status)
            .map { announcementMapper.toDomain(it) }
    }

    override fun existsByUrl(url: String): Boolean {
        return announcementJpaRepository.existsByUrl(url)
    }

    // SaveAnnouncementPort implementation
    override fun save(announcement: Announcement): Announcement {
        val jpaEntity = if (announcement.id != null) {
            announcementJpaRepository.findByIdOrNull(announcement.id)
                ?.also { announcementMapper.updateJpaEntity(it, announcement) }
                ?: announcementMapper.toJpaEntity(announcement)
        } else {
            announcementMapper.toJpaEntity(announcement)
        }

        val saved = announcementJpaRepository.save(jpaEntity)
        return announcementMapper.toDomain(saved)
    }

    // LoadAnnouncementLikePort implementations
    override fun existsByUserIdAndAnnouncementId(userId: Long, announcementId: Long): Boolean {
        return announcementLikeJpaRepository.existsByUserIdAndAnnouncementId(userId, announcementId)
    }

    override fun loadByUserIdAndAnnouncementId(userId: Long, announcementId: Long): AnnouncementLike? {
        return announcementLikeJpaRepository.findByUserIdAndAnnouncementId(userId, announcementId)
            ?.let { announcementLikeMapper.toDomain(it) }
    }

    override fun loadByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<AnnouncementLike> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User not found: $userId")
        return announcementLikeJpaRepository.findByUserOrderByCreatedAtDesc(user, pageable)
            .map { announcementLikeMapper.toDomain(it) }
    }

    // SaveAnnouncementLikePort implementation
    override fun save(announcementLike: AnnouncementLike): AnnouncementLike {
        val user = userRepository.findByIdOrNull(announcementLike.userId)
            ?: throw IllegalArgumentException("User not found: ${announcementLike.userId}")
        val announcement = announcementJpaRepository.findByIdOrNull(announcementLike.announcementId)
            ?: throw IllegalArgumentException("Announcement not found: ${announcementLike.announcementId}")

        val jpaEntity = announcementLikeMapper.toJpaEntity(announcementLike, user, announcement)
        val saved = announcementLikeJpaRepository.save(jpaEntity)
        return announcementLikeMapper.toDomain(saved)
    }

    // DeleteAnnouncementLikePort implementation
    override fun delete(announcementLike: AnnouncementLike) {
        val jpaEntity = announcementLikeJpaRepository.findByUserIdAndAnnouncementId(
            announcementLike.userId,
            announcementLike.announcementId
        ) ?: return
        announcementLikeJpaRepository.delete(jpaEntity)
    }
}
