package com.jininsadaecheonmyeong.starthubserver.domain.user.docs

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.DeleteUserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserProfileResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserResponse
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "유저", description = "사용자 관련 API")
interface UserDocs {
    @Operation(summary = "회원가입", description = "이메일 인증을 먼저 해야합니다.")
    fun signUp(
        @RequestBody request: UserRequest,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(
        summary = "로그인",
        description = "X-Platform 헤더가 'web'이면 쿠키로 토큰을 설정하고, 'app'이면 JSON으로 토큰을 반환합니다.",
    )
    fun signIn(
        @RequestBody request: UserRequest,
        @Parameter(hidden = true) httpRequest: HttpServletRequest,
        @Parameter(hidden = true) httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Any>>

    @Operation(
        summary = "토큰 재발급",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. X-Platform 헤더에 따라 쿠키 또는 JSON으로 응답합니다.",
    )
    fun reissue(
        @RequestBody request: RefreshRequest,
        @Parameter(hidden = true) httpRequest: HttpServletRequest,
        @Parameter(hidden = true) httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Any>>

    @Operation(summary = "유저 프로필 업데이트", description = "최초 로그인 시 또는 마이페이지에서 사용자 프로필 정보를 업데이트합니다.")
    fun updateUserProfile(
        @RequestBody request: UpdateUserProfileRequest,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "유저 정보 조회", description = "현재 로그인한 유저의 정보를 조회합니다.")
    fun getUser(): ResponseEntity<BaseResponse<UserResponse>>

    @Operation(summary = "로그아웃", description = "현재 로그인한 유저를 로그아웃 처리합니다.")
    fun signOut(
        @Parameter(hidden = true) httpRequest: HttpServletRequest,
        @Parameter(hidden = true) httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Unit>>

    @Operation(summary = "유저 프로필 조회", description = "유저 ID로 프로필을 조회합니다.")
    fun getUserProfile(
        @Parameter(description = "유저 ID") @PathVariable userId: Long,
    ): ResponseEntity<BaseResponse<UserProfileResponse>>

    @Operation(
        summary = "회원 탈퇴",
        description = "LOCAL 사용자는 비밀번호를 확인한 후, OAuth 사용자는 비밀번호 없이 회원 탈퇴를 진행합니다. 탈퇴 후 2주 이내에 다시 로그인하면 탈퇴가 철회됩니다.",
    )
    fun deleteAccount(
        @RequestBody request: DeleteUserRequest,
        @Parameter(hidden = true) httpRequest: HttpServletRequest,
        @Parameter(hidden = true) httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Unit>>
}
