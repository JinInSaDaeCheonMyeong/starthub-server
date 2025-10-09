package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.entity.AnnouncementLike
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.LikeNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AnnouncementLikeService(
    private val announcementLikeRepository: AnnouncementLikeRepository,
    private val announcementRepository: AnnouncementRepository,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) {
    fun addLike(announcementId: Long) {
        val user = userAuthenticationHolder.current()
        val announcement =
            announcementRepository.findByIdOrNull(announcementId) ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        if (announcementLikeRepository.existsByUserAndAnnouncement(user, announcement)) {
            throw LikeAlreadyExistsException("이미 좋아요를 누른 공고")
        }

        val like =
            AnnouncementLike(
                user = user,
                announcement = announcement,
            )

        announcementLikeRepository.save(like)

        announcement.likeCount++
        announcementRepository.save(announcement)
    }

    fun removeLike(announcementId: Long) {
        val user = userAuthenticationHolder.current()
        val announcement =
            announcementRepository.findByIdOrNull(announcementId) ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        val like =
            announcementLikeRepository.findByUserAndAnnouncement(user, announcement)
                ?: throw LikeNotFoundException("좋아요를 누르지 않은 공고")

        announcementLikeRepository.delete(like)

        announcement.likeCount--
        announcementRepository.save(announcement)
    }
}
