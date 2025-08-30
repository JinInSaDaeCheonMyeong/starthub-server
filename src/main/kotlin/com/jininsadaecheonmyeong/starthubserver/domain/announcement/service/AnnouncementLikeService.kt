package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AnnouncementLikeService(
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
) {
    fun addLike(userId: Long, announcementId: Long) {
        // TODO: User and Announcement not found exception
        val user = userRepository.findById(userId).orElseThrow()
        val announcement = announcementRepository.findById(announcementId).orElseThrow()

        if (announcementLikeRepository.existsByUserAndAnnouncement(user, announcement)) {
            // TODO: Like already exists exception
            throw RuntimeException("Like already exists")
        }

        announcement.likeCount++
        announcementRepository.save(announcement)
    }

    fun removeLike(userId: Long, announcementId: Long) {
        // TODO: User and Announcement not found exception
        val user = userRepository.findById(userId).orElseThrow()
        val announcement = announcementRepository.findById(announcementId).orElseThrow()

        val like = announcementLikeRepository.findByUserAndAnnouncement(user, announcement)
            ?: throw RuntimeException("Like not found") // TODO: Like not found exception

        announcementLikeRepository.delete(like)

        announcement.likeCount--
        announcementRepository.save(announcement)
    }
}
