package com.jininsadaecheonmyeong.starthubserver.domain.notification.docs

import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.request.RegisterFcmTokenRequest
import com.jininsadaecheonmyeong.starthubserver.domain.notification.data.response.FcmTokenResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Notification", description = "푸시 알림 API")
interface NotificationDocs {
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    fun registerFcmToken(request: RegisterFcmTokenRequest): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다.")
    fun deleteFcmToken(token: String): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "내 FCM 토큰 조회", description = "현재 사용자의 모든 FCM 토큰을 조회합니다.")
    fun getMyFcmTokens(): ResponseEntity<BaseResponse<List<FcmTokenResponse>>>
}
