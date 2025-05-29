package com.jininsadaecheonmyeong.starthubserver.domain.user.docs

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.UserRequest
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
}