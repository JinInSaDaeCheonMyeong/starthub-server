package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.GetScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.LoadSchedulePort
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetScheduleService(
    private val loadSchedulePort: LoadSchedulePort
) : GetScheduleUseCase {

    override fun getSchedulesByMonth(userId: Long, date: LocalDate): List<Schedule> {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        return loadSchedulePort.loadByMonth(userId, startOfMonth, endOfMonth)
    }

    override fun getSchedulesByDate(userId: Long, date: LocalDate): List<Schedule> {
        return loadSchedulePort.loadByDate(userId, date)
    }
}
