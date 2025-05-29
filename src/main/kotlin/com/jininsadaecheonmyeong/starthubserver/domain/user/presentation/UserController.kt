package com.jininsadaecheonmyeong.starthubserver.domain.user.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.docs.UserDocs
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.UserService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
) : UserDocs {
    @PostMapping("/sign-up")
    override fun signUp(request: UserRequest) =
        BaseResponse.created(userService.signUp(request), "회원가입 성공")

    @PostMapping("/sign-in")
    override fun signIn(request: UserRequest) =
        BaseResponse.ok(userService.signIn(request), "로그인 성공")

    @PostMapping("/reissue")
    override fun reissue(request: RefreshRequest) =
        BaseResponse.ok(userService.reissue(request), "토큰 재발급 성공")
}