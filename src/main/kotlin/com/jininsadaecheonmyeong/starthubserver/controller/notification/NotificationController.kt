package com.jininsadaecheonmyeong.starthubserver.controller.notification

import com.jininsadaecheonmyeong.starthubserver.docs.notification.NotificationDocs
import com.jininsadaecheonmyeong.starthubserver.dto.request.notification.RegisterFcmTokenRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.notification.TestNotificationRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.NotificationHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.usecase.notification.NotificationUseCase
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
    private val notificationUseCase: NotificationUseCase,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : NotificationDocs {
    @PostMapping("/fcm-token")
    override fun registerFcmToken(
        @Valid @RequestBody request: RegisterFcmTokenRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        notificationUseCase.registerToken(user, request.token, request.deviceType)
        return BaseResponse.of(Unit, HttpStatus.CREATED, "FCM 토큰 등록 성공")
    }

    @DeleteMapping("/fcm-token")
    override fun deleteFcmToken(
        @RequestParam token: String,
    ): ResponseEntity<BaseResponse<Unit>> {
        notificationUseCase.deleteToken(token)
        return BaseResponse.of(Unit, HttpStatus.OK, "FCM 토큰 삭제 성공")
    }

    @GetMapping("/fcm-tokens")
    override fun getMyFcmTokens(): ResponseEntity<BaseResponse<List<FcmTokenResponse>>> {
        val user = userAuthenticationHolder.current()
        val tokens = notificationUseCase.getTokensByUser(user)
        return BaseResponse.of(tokens, HttpStatus.OK, "FCM 토큰 조회 성공")
    }

    @GetMapping("/history")
    override fun getNotificationHistory(): ResponseEntity<BaseResponse<List<NotificationHistoryResponse>>> {
        val user = userAuthenticationHolder.current()
        val histories = notificationUseCase.getNotificationHistory(user)
        return BaseResponse.of(histories, HttpStatus.OK, "알림 히스토리 조회 성공")
    }

    @PostMapping("/test")
    override fun sendTestNotification(
        @Valid @RequestBody request: TestNotificationRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val user = userAuthenticationHolder.current()
        notificationUseCase.sendPushNotificationToUser(user, request.title, request.body, request.data)
        return BaseResponse.of(Unit, HttpStatus.OK, "테스트 알림 전송 성공")
    }
}
