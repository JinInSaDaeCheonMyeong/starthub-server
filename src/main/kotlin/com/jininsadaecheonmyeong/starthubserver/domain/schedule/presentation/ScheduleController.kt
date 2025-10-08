package com.jininsadaecheonmyeong.starthubserver.domain.schedule.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.request.ScheduleRequest
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.DailyScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.ScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.docs.ScheduleDocs
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.service.ScheduleService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : ScheduleDocs {
    @PostMapping
    override fun createSchedule(
        @RequestBody request: ScheduleRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        scheduleService.createSchedule(user.id!!, request)
        return BaseResponse.of(Unit, HttpStatus.CREATED, "일정 생성 성공")
    }

    @DeleteMapping
    override fun deleteSchedule(@RequestParam announcementId: Long): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        scheduleService.deleteSchedule(user.id!!, announcementId)
        return BaseResponse.of(Unit, HttpStatus.OK, "일정 삭제 성공")
    }

    @GetMapping("/month")
    override fun getSchedulesByMonth(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<BaseResponse<List<ScheduleResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = scheduleService.getSchedulesByMonth(user.id!!, date)
        return BaseResponse.of(schedules, HttpStatus.OK, "월별 일정 조회 성공")
    }

    @GetMapping("/date")
    override fun getSchedulesByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<BaseResponse<List<DailyScheduleResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = scheduleService.getSchedulesByDate(user.id!!, date)
        return BaseResponse.of(schedules, HttpStatus.OK, "일별 일정 조회 성공")
    }
}
