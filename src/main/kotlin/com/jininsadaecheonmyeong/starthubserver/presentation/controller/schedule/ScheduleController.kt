package com.jininsadaecheonmyeong.starthubserver.presentation.controller.schedule

import com.jininsadaecheonmyeong.starthubserver.application.usecase.schedule.ScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.schedule.ScheduleDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.schedule.ScheduleRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.schedule.DailyScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.schedule.ScheduleResponse
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
    private val scheduleUseCase: ScheduleUseCase,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : ScheduleDocs {
    @PostMapping
    override fun createSchedule(
        @RequestBody request: ScheduleRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        scheduleUseCase.createSchedule(user.id!!, request)
        return BaseResponse.of(Unit, HttpStatus.CREATED, "일정 생성 성공")
    }

    @DeleteMapping
    override fun deleteSchedule(
        @RequestParam announcementId: Long,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        scheduleUseCase.deleteSchedule(user.id!!, announcementId)
        return BaseResponse.of(Unit, HttpStatus.OK, "일정 삭제 성공")
    }

    @GetMapping("/month")
    override fun getSchedulesByMonth(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<BaseResponse<List<ScheduleResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = scheduleUseCase.getSchedulesByMonth(user.id!!, date)
        return BaseResponse.of(schedules, HttpStatus.OK, "월별 일정 조회 성공")
    }

    @GetMapping("/date")
    override fun getSchedulesByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<BaseResponse<List<DailyScheduleResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = scheduleUseCase.getSchedulesByDate(user.id!!, date)
        return BaseResponse.of(schedules, HttpStatus.OK, "일별 일정 조회 성공")
    }
}
