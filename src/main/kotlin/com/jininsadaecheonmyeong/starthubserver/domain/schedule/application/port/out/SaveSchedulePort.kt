package com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.out

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.domain.model.Schedule

interface SaveSchedulePort {
    fun save(schedule: Schedule): Schedule
}
