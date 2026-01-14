package com.jininsadaecheonmyeong.starthubserver.docs.notification

import com.jininsadaecheonmyeong.starthubserver.dto.request.notification.RegisterFcmTokenRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.notification.TestNotificationRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.notification.NotificationHistoryResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "푸시 알림", description = "푸시 알림 API")
interface NotificationDocs {
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    fun registerFcmToken(request: RegisterFcmTokenRequest): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다.")
    fun deleteFcmToken(token: String): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "내 FCM 토큰 조회", description = "현재 사용자의 모든 FCM 토큰을 조회합니다.")
    fun getMyFcmTokens(): ResponseEntity<BaseResponse<List<FcmTokenResponse>>>

    @Operation(summary = "알림 히스토리 조회", description = "현재 사용자가 받은 모든 알림 히스토리를 최신순으로 조회합니다.")
    fun getNotificationHistory(): ResponseEntity<BaseResponse<List<NotificationHistoryResponse>>>

    @Operation(summary = "테스트 알림 전송", description = "테스트용 FCM 푸시 알림을 전송합니다.")
    fun sendTestNotification(request: TestNotificationRequest): ResponseEntity<BaseResponse<Unit>>
}
