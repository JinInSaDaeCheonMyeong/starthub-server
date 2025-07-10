package com.jininsadaecheonmyeong.starthubserver.domain.user.docs

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UserRequest
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "유저", description = "사용자 관련 API")
interface UserDocs {

    @Operation(summary = "회원가입", description = "이메일 인증을 먼저 해야합니다.")
    fun signUp(@RequestBody request: UserRequest): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 JWT 토큰을 반환합니다.")
    fun signIn(@RequestBody request: UserRequest): ResponseEntity<BaseResponse<TokenResponse>>

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    fun reissue(@RequestBody request: RefreshRequest): ResponseEntity<BaseResponse<TokenResponse>>

    @Operation(summary = "유저 프로필 업데이트", description = "최초 로그인 시 또는 마이페이지에서 사용자 프로필 정보를 업데이트합니다.")
    fun updateUserProfile(@RequestBody request: UpdateUserProfileRequest): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "유저 정보 조회", description = "현재 로그인한 유저의 정보를 조회합니다.")
    fun getUser(): ResponseEntity<BaseResponse<UserResponse>>
}