package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
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
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
        val announcement =
            announcementRepository.findById(announcementId).orElseThrow { AnnouncementNotFoundException("찾을 수 없는 공고") }

        if (announcementLikeRepository.existsByUserAndAnnouncement(user, announcement)) {
            throw LikeAlreadyExistsException("이미 좋아요를 누른 공고")
        }

        val like = AnnouncementLike(
            user = user,
            announcement = announcement
        )

        announcementLikeRepository.save(like)

        announcement.likeCount++
        announcementRepository.save(announcement)
    }

    fun removeLike(userId: Long, announcementId: Long) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
        val announcement =
            announcementRepository.findById(announcementId).orElseThrow { AnnouncementNotFoundException("찾을 수 없는 공고") }

        val like = announcementLikeRepository.findByUserAndAnnouncement(user, announcement)
            ?: throw LikeNotFoundException("좋아요를 누르지 않은 공고")

        announcementLikeRepository.delete(like)

        announcement.likeCount--
        announcementRepository.save(announcement)
    }
}
