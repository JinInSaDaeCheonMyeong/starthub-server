package com.jininsadaecheonmyeong.starthubserver.domain.notification.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request.RegisterFcmTokenRequest
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request.TestNotificationRequest
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response.NotificationHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.domain.notification.docs.NotificationDocs
import com.jininsadaecheonmyeong.starthubserver.domain.notification.service.FcmService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val fcmService: FcmService,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : NotificationDocs {
    @PostMapping("/fcm-token")
    override fun registerFcmToken(
        @Valid @RequestBody request: RegisterFcmTokenRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        fcmService.registerToken(user, request.token, request.deviceType)
        return BaseResponse.of(Unit, HttpStatus.CREATED, "FCM 토큰 등록 성공")
    }

    @DeleteMapping("/fcm-token")
    override fun deleteFcmToken(
        @RequestParam token: String,
    ): ResponseEntity<BaseResponse<Unit>> {
        fcmService.deleteToken(token)
        return BaseResponse.of(Unit, HttpStatus.OK, "FCM 토큰 삭제 성공")
    }

    @GetMapping("/fcm-tokens")
    override fun getMyFcmTokens(): ResponseEntity<BaseResponse<List<FcmTokenResponse>>> {
        val user = userAuthenticationHolder.current()
        val tokens = fcmService.getTokensByUser(user)
        return BaseResponse.of(tokens, HttpStatus.OK, "FCM 토큰 조회 성공")
    }

    @GetMapping("/history")
    override fun getNotificationHistory(): ResponseEntity<BaseResponse<List<NotificationHistoryResponse>>> {
        val user = userAuthenticationHolder.current()
        val histories = fcmService.getNotificationHistory(user)
        return BaseResponse.of(histories, HttpStatus.OK, "알림 히스토리 조회 성공")
    }

    @PostMapping("/test")
    override fun sendTestNotification(
        @Valid @RequestBody request: TestNotificationRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        fcmService.sendPushNotificationToUser(user, request.title, request.body, request.data)
        return BaseResponse.of(Unit, HttpStatus.OK, "테스트 알림 전송 성공")
    }
}
