package com.jininsadaecheonmyeong.starthubserver.domain.user.presentation

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.docs.UserDocs
import com.jininsadaecheonmyeong.starthubserver.domain.user.service.UserService
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
) : UserDocs {
    @PostMapping("/sign-up")
    override fun signUp(@Valid request: UserRequest) =
        BaseResponse.of(userService.signUp(request), "회원가입 성공")

    @PostMapping("/sign-in")
    override fun signIn(request: UserRequest) =
        BaseResponse.of(userService.signIn(request), "로그인 성공")

    @PostMapping("/reissue")
    override fun reissue(request: RefreshRequest) =
        BaseResponse.of(userService.reissue(request), "토큰 재발급 성공")

    @PatchMapping("/profile")
    override fun updateUserProfile(@RequestBody request: UpdateUserProfileRequest): ResponseEntity<BaseResponse<Unit>> {
        val currentUser = UserAuthenticationHolder.current()
        userService.updateUserProfile(currentUser, request)
        return BaseResponse.of("유저 프로필 설정 성공")
    }

    @GetMapping("/me")
    override fun getUser(): ResponseEntity<BaseResponse<UserResponse>> {
        val currentUser = UserAuthenticationHolder.current()
        val user = userService.getUser(currentUser)
        return BaseResponse.of(user, "유저 정보 조회 성공")
    }
}