package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.service

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.DeleteScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out.DeleteSchedulePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeleteScheduleService(
    private val deleteSchedulePort: DeleteSchedulePort
) : DeleteScheduleUseCase {

    override fun deleteSchedule(userId: Long, announcementId: Long) {
        deleteSchedulePort.deleteByAnnouncementIdAndUserId(announcementId, userId)
    }
}
