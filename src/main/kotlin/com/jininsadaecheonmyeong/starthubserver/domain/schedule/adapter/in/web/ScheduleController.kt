package com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.enums.AnnouncementStatus
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementLikeRepository
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.repository.AnnouncementRepository
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web.request.ScheduleWebRequest
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web.response.DailyScheduleWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.adapter.`in`.web.response.ScheduleWebResponse
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.CreateScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.DeleteScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.application.port.`in`.GetScheduleUseCase
import com.jininsadaecheonmyeong.starthubserver.domain.schedule.docs.ScheduleDocs
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val userAuthenticationHolder: UserAuthenticationHolder,
    private val announcementRepository: AnnouncementRepository,
    private val announcementLikeRepository: AnnouncementLikeRepository
) : ScheduleDocs {

    @PostMapping
    override fun createSchedule(
        @RequestBody request: ScheduleWebRequest
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        createScheduleUseCase.createSchedule(request.toCommand(user.id!!))
        return BaseResponse.of(Unit, HttpStatus.CREATED, "일정 생성 성공")
    }

    @DeleteMapping
    override fun deleteSchedule(
        @RequestParam announcementId: Long
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        deleteScheduleUseCase.deleteSchedule(user.id!!, announcementId)
        return BaseResponse.of(Unit, HttpStatus.OK, "일정 삭제 성공")
    }

    @GetMapping("/month")
    override fun getSchedulesByMonth(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<BaseResponse<List<ScheduleWebResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = getScheduleUseCase.getSchedulesByMonth(user.id!!, date)

        // Announcement 정보와 결합
        val responses = schedules.mapNotNull { schedule ->
            announcementRepository.findById(schedule.announcementId).orElse(null)
                ?.takeIf { it.status == AnnouncementStatus.ACTIVE }
                ?.let { announcement ->
                    ScheduleWebResponse(
                        announcementId = announcement.id!!,
                        supportField = announcement.supportField,
                        startDate = schedule.startDate,
                        endDate = schedule.endDate
                    )
                }
        }
        return BaseResponse.of(responses, HttpStatus.OK, "월별 일정 조회 성공")
    }

    @GetMapping("/date")
    override fun getSchedulesByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<BaseResponse<List<DailyScheduleWebResponse>>> {
        val user = userAuthenticationHolder.current()
        val schedules = getScheduleUseCase.getSchedulesByDate(user.id!!, date)

        // Announcement 정보와 결합
        val announcements = schedules.mapNotNull { schedule ->
            announcementRepository.findById(schedule.announcementId).orElse(null)
                ?.takeIf { it.status == AnnouncementStatus.ACTIVE }
        }

        val likedAnnouncementIds = announcementLikeRepository
            .findAllByUserIdAndAnnouncementIn(user.id!!, announcements)
            .map { it.announcement.id }
            .toSet()

        val responses = announcements.map { announcement ->
            DailyScheduleWebResponse(
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
                isLiked = likedAnnouncementIds.contains(announcement.id)
            )
        }

        return BaseResponse.of(responses, HttpStatus.OK, "일별 일정 조회 성공")
    }
}
