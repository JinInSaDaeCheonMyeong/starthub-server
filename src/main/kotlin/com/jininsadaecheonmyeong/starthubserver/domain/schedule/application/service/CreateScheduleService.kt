package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.CreateScheduleCommand
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.CreateScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.SaveSchedulePort
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateScheduleService(
    private val saveSchedulePort: SaveSchedulePort
) : CreateScheduleUseCase {

    override fun createSchedule(command: CreateScheduleCommand): Schedule {
        val schedule = Schedule.create(
            userId = command.userId,
            announcementId = command.announcementId,
            startDate = command.startDate,
            endDate = command.endDate
        )
        return saveSchedulePort.save(schedule)
    }
}
