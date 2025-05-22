package com.jininsadaecheonmyeong.starthubserver.domain.user.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.UserService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
@Tag(name = "유저", description = "사용자 관련 API")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "회원가입", description = "이메일 인증을 먼저 해야합니다.")
    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: UserRequest)
        = BaseResponse.created(userService.signUp(request), "회원가입 성공")

    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 JWT 토큰을 반환합니다.")
    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: UserRequest)
        = BaseResponse.ok(userService.signIn(request), "로그인 성공")

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/reissue")
    fun reissue(@RequestBody request: RefreshRequest)
        = BaseResponse.ok(userService.reissue(request), "토큰 재발급 성공")
}