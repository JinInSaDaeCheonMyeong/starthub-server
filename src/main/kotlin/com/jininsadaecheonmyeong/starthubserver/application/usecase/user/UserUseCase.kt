package com.jininsadaecheonmyeong.starthubserver.application.usecase.user

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.DeleteUserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.UserProfileResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.UserResponse

interface UserUseCase {
    fun signUp(request: UserRequest)

    fun signIn(request: UserRequest): TokenResponse

    fun reissue(request: RefreshRequest): TokenResponse

    fun updateUserProfile(
        user: User,
        request: UpdateUserProfileRequest,
    )

    fun getUser(user: User): UserResponse

    fun signOut(user: User)

    fun deleteAccount(
        user: User,
        request: DeleteUserRequest,
    )

    fun getUserProfile(userId: Long): UserProfileResponse
}
