package com.jininsadaecheonmyeong.starthubserver.usecase.user

import com.jininsadaecheonmyeong.starthubserver.dto.request.user.DeleteUserRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.user.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.user.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.dto.request.user.UserRequest
import com.jininsadaecheonmyeong.starthubserver.dto.response.user.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.user.UserProfileResponse
import com.jininsadaecheonmyeong.starthubserver.dto.response.user.UserResponse
import com.jininsadaecheonmyeong.starthubserver.entity.user.User

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
