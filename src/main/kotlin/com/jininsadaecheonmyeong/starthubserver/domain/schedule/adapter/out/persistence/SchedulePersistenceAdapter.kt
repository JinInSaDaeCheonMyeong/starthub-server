package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception.AnnouncementNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.mapper.SchedulePersistenceMapper
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.out.persistence.repository.ScheduleJpaRepository
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.DeleteSchedulePort
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.LoadSchedulePort
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.SaveSchedulePort
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SchedulePersistenceAdapter(
    private val jpaRepository: ScheduleJpaRepository,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val mapper: SchedulePersistenceMapper
) : LoadSchedulePort, SaveSchedulePort, DeleteSchedulePort {

    override fun loadByMonth(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Schedule> {
        return jpaRepository.findSchedulesByMonth(userId, startDate, endDate)
            .map { mapper.toDomain(it) }
    }

    override fun loadByDate(userId: Long, date: LocalDate): List<Schedule> {
        return jpaRepository.findByUserIdAndDate(userId, date)
            .map { mapper.toDomain(it) }
    }

    override fun save(schedule: Schedule): Schedule {
        val user = userRepository.findById(schedule.userId).orElse(null)
            ?: throw UserNotFoundException("찾을 수 없는 유저")
        val announcement = announcementRepository.findById(schedule.announcementId).orElse(null)
            ?: throw AnnouncementNotFoundException("찾을 수 없는 공고")

        val jpaEntity = mapper.toJpaEntity(schedule, user, announcement)
        val saved = jpaRepository.save(jpaEntity)
        return mapper.toDomain(saved)
    }

    override fun deleteByAnnouncementIdAndUserId(announcementId: Long, userId: Long) {
        jpaRepository.deleteByAnnouncementIdAndUserId(announcementId, userId)
    }
}
