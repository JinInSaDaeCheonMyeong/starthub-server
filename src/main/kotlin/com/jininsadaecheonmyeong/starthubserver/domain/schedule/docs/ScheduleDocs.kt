package com.jininsadaecheonmyeong.starthubserver.domain.schedule.docs
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.request.ScheduleRequest
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.AnnouncementSummaryResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.data.response.ScheduleResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
@Tag(name = "Schedules", description = "일정 API")
interface ScheduleDocs {
    @Operation(summary = "일정 생성", description = "새로운 일정을 생성합니다.")
    fun createSchedule(@RequestBody request: ScheduleRequest): BaseResponse<Unit>

    @Operation(summary = "월별 일정 조회", description = "특정 월의 일정 목록을 조회합니다.")
    fun getSchedulesByMonth(
        @Parameter(description = "조회할 월의 아무 날짜") @RequestParam date: LocalDate
    ): BaseResponse<List<ScheduleResponse>>

    @Operation(summary = "일별 일정 조회", description = "특정 날짜의 일정 목록을 조회합니다.")
    fun getSchedulesByDate(
        @Parameter(description = "조회할 날짜") @RequestParam date: LocalDate
    ): BaseResponse<List<AnnouncementSummaryResponse>>
}
