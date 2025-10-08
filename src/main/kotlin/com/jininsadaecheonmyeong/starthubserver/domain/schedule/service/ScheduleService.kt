package com.jininsadaecheonmyeong.starthubserver.domain.schedule.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.request.ScheduleRequest
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.DailyScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.ScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.entity.Schedule
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.repository.ScheduleRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository,
) {
    @Transactional
    fun createSchedule(
        userId: Long,
        request: ScheduleRequest,
    ) {
        val user =
            userRepository.findById(userId)
                .orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
        val announcement =
            announcementRepository.findById(request.announcementId)
                .orElseThrow { AnnouncementNotFoundException("찾을 수 없는 공고") }

        val schedule =
            Schedule(
                user = user,
                announcement = announcement,
                startDate = request.startDate,
                endDate = request.endDate,
            )

        scheduleRepository.save(schedule)
    }

    @Transactional
    fun deleteSchedule(
        userId: Long,
        announcementId: Long,
    ) {
        scheduleRepository.deleteByAnnouncementIdAndUserId(announcementId, userId)
    }

    @Transactional(readOnly = true)
    fun getSchedulesByMonth(
        userId: Long,
        date: LocalDate,
    ): List<ScheduleResponse> {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())

        return scheduleRepository.findSchedulesByMonth(userId, startOfMonth, endOfMonth)
            .filter { it.announcement.status == AnnouncementStatus.ACTIVE }
            .map { ScheduleResponse(it.announcement.id!!, it.announcement.supportField, it.startDate, it.endDate) }
    }

    @Transactional(readOnly = true)
    fun getSchedulesByDate(
        userId: Long,
        date: LocalDate,
    ): List<DailyScheduleResponse> {
        val schedules =
            scheduleRepository.findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, date, date)
                .filter { it.announcement.status == AnnouncementStatus.ACTIVE }
        val announcements = schedules.map { it.announcement }
        val likedAnnouncementIds =
            announcementLikeRepository.findAllByUserIdAndAnnouncementIn(userId, announcements).map { it.announcement.id }
                .toSet()

        return announcements.map { announcement ->
            DailyScheduleResponse(
                id = announcement.id!!,
                title = announcement.title,
                url = announcement.url,
                organization = announcement.organization,
                receptionPeriod = announcement.receptionPeriod,
                likeCount = announcement.likeCount,
                supportField = announcement.supportField,
                targetAge = announcement.targetAge,
                contactNumber = announcement.contactNumber,
                region = announcement.region,
                organizationType = announcement.organizationType,
                startupHistory = announcement.startupHistory,
                departmentInCharge = announcement.departmentInCharge,
                content = announcement.content,
                isLiked = likedAnnouncementIds.contains(announcement.id),
            )
        }
    }
}
