package com.jininsadaecheonmyeong.starthubserver.presentation.controller.user

import com.jininsadaecheonmyeong.starthubserver.application.usecase.user.UserUseCase
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.presentation.docs.user.UserDocs
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.DeleteUserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.UserResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userUseCase: UserUseCase,
    private val userAuthenticationHolder: UserAuthenticationHolder,
) : UserDocs {
    @PostMapping("/sign-up")
    override fun signUp(
        @Valid @RequestBody request: UserRequest,
    ) = BaseResponse.of(userUseCase.signUp(request), "회원가입 성공")

    @PostMapping("/sign-in")
    override fun signIn(
        @RequestBody request: UserRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
    ) = BaseResponse.of(userUseCase.signIn(request), "로그인 성공")

    @PostMapping("/reissue")
    override fun reissue(
        @RequestBody request: RefreshRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
    ) = BaseResponse.of(userUseCase.reissue(request), "토큰 재발급 성공")

    @PatchMapping("/profile")
    override fun updateUserProfile(
        @RequestBody request: UpdateUserProfileRequest,
    ): ResponseEntity<BaseResponse<Unit>> {
        val currentUser = userAuthenticationHolder.current()
        userUseCase.updateUserProfile(currentUser, request)
        return BaseResponse.of("유저 프로필 설정 성공")
    }

    @GetMapping("/me")
    override fun getUser(): ResponseEntity<BaseResponse<UserResponse>> {
        val currentUser = userAuthenticationHolder.current()
        val user = userUseCase.getUser(currentUser)
        return BaseResponse.of(user, "유저 정보 조회 성공")
    }

    @PostMapping("/sign-out")
    override fun signOut(
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Unit>> {
        val currentUser = userAuthenticationHolder.current()
        userUseCase.signOut(currentUser)

        return BaseResponse.of("로그아웃 성공")
    }

    @GetMapping("/{userId}/profile")
    override fun getUserProfile(
        @PathVariable userId: Long,
    ) = BaseResponse.of(userUseCase.getUserProfile(userId), "유저 프로필 조회 성공")

    @DeleteMapping
    override fun deleteAccount(
        @RequestBody request: DeleteUserRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<BaseResponse<Unit>> {
        val currentUser = userAuthenticationHolder.current()
        userUseCase.deleteAccount(currentUser, request)

        return BaseResponse.of("회원 탈퇴가 완료되었습니다.")
    }
}
