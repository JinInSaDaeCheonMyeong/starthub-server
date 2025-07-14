package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailNotVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.email.repository.EmailRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.request.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.ProfileImageResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.response.UserResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.UserInterest
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserInterestRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.common.BaseResponse
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenRedisService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.support.UserAuthenticationHolder
import com.jininsadaecheonmyeong.starthubserver.global.service.GcsService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser,
    private val tokenRedisService: TokenRedisService,
    private val emailRepository: EmailRepository,
    private val userInterestRepository: UserInterestRepository,
    private val gcsService: GcsService,
) {
    fun signUp(request: UserRequest) {
        if (userRepository.existsByEmail(request.email)) throw EmailAlreadyExistsException("이미 등록된 이메일")
        val verification = emailRepository.findByEmail(request.email)
        if (verification == null || !verification.isVerified) throw EmailNotVerifiedException("인증되지 않은 이메일")
        userRepository.save(request.toEntity(passwordEncoder.encode(request.password)))
    }

    fun signIn(request: UserRequest): TokenResponse {
        val user: User = userRepository.findByEmail(request.email) ?: throw UserNotFoundException("찾을 수 없는 유저")
        if (!passwordEncoder.matches(request.password, user.password)) throw InvalidPasswordException("잘못된 비밀번호")
        val isFirstLogin = user.isFirstLogin
        if (isFirstLogin) {
            user.isFirstLogin = false
            userRepository.save(user)
        }
        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
            isFirstLogin = isFirstLogin,
        )
    }

    fun reissue(request: RefreshRequest): TokenResponse {
        val email: String = tokenParser.findEmail(request.refresh)
        tokenValidator.validateAll(request.refresh, TokenType.REFRESH_TOKEN)
        val user: User =
            userRepository.findByEmail(tokenParser.findEmail(request.refresh))
                ?: throw UserNotFoundException("찾을 수 없는 유저")

        if (tokenRedisService.findByEmail(email)?.equals(request.refresh) != true) throw InvalidTokenException("유효하지 않은 리프레시 토큰")

        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
            isFirstLogin = false,
        )
    }

    fun updateUserProfile(
        user: User,
        request: UpdateUserProfileRequest,
    ) {
        user.username = request.username
        user.introduction = request.introduction
        user.birth = request.birth
        user.gender = request.gender
        user.profileImage = request.profileImage
        userRepository.save(user)

        userInterestRepository.deleteByUser(user)

        val newInterests =
            request.interests.map { interestType ->
                UserInterest(user = user, businessType = interestType)
            }
        userInterestRepository.saveAll(newInterests)
    }

    @Transactional(readOnly = true)
    fun getUser(user: User): UserResponse {
        return UserResponse(user)
    }

    fun updateProfileImage(
        user: User,
        imageUrl: String,
    ) {
        user.profileImage = imageUrl
        userRepository.save(user)
    }

    fun uploadProfileImage(
        file: MultipartFile,
    ): ResponseEntity<BaseResponse<ProfileImageResponse>> {
        val currentUser = UserAuthenticationHolder.current()
        val imageUrl = gcsService.uploadFile(file, "profile-images")
        updateProfileImage(currentUser, imageUrl)
        val response = ProfileImageResponse(imageUrl)
        return BaseResponse.of(response, "프로필 이미지 업로드 성공")
    }
}
