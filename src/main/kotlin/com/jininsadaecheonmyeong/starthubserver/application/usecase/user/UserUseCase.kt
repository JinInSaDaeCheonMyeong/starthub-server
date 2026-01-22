package com.jininsadaecheonmyeong.starthubserver.application.usecase.user

import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.UserStartupField
import com.jininsadaecheonmyeong.starthubserver.domain.enums.user.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.exception.email.EmailNotVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.exception.user.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.repository.company.CompanyRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.email.EmailRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserRepository
import com.jininsadaecheonmyeong.starthubserver.domain.repository.user.UserStartupFieldRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenRedisService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import com.jininsadaecheonmyeong.starthubserver.global.security.token.service.TokenService
import com.jininsadaecheonmyeong.starthubserver.infrastructure.cache.user.UserCache
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.DeleteUserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UpdateUserProfileRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.request.user.UserRequest
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.StartupFieldResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.UserProfileResponse
import com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.user.UserResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional
class UserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser,
    private val tokenRedisService: TokenRedisService,
    private val emailRepository: EmailRepository,
    private val userStartupFieldRepository: UserStartupFieldRepository,
    private val companyRepository: CompanyRepository,
    private val tokenService: TokenService,
    private val userCache: UserCache,
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

        var isAccountRestored = false
        var originalDeletedAt: LocalDateTime? = null
        var needsCacheUpdate = false

        if (user.deleted && user.deletedAt != null) {
            val twoWeeksAfterDeletion = user.deletedAt!!.plusWeeks(2)
            if (LocalDateTime.now().isBefore(twoWeeksAfterDeletion)) {
                originalDeletedAt = user.deletedAt
                user.deleted = false
                user.deletedAt = null
                isAccountRestored = true
                needsCacheUpdate = true
            } else {
                throw UserNotFoundException("탈퇴 처리된 계정입니다.")
            }
        } else if (user.deleted) {
            throw UserNotFoundException("탈퇴 처리된 계정입니다.")
        }

        val isFirstLogin = user.isFirstLogin
        if (isFirstLogin) {
            user.isFirstLogin = false
            needsCacheUpdate = true
        }

        if (needsCacheUpdate) {
            userRepository.save(user)
            userCache.put(user)
        }

        return TokenResponse(
            access = tokenService.generateAccess(user),
            refresh = tokenService.generateAndStoreRefreshToken(user),
            isFirstLogin = isFirstLogin,
            isAccountRestored = isAccountRestored,
            deletedAt = originalDeletedAt,
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
            access = tokenService.generateAccess(user),
            refresh = tokenService.generateAndStoreRefreshToken(user),
            isFirstLogin = false,
            isAccountRestored = false,
            deletedAt = null,
        )
    }

    fun updateUserProfile(
        user: User,
        request: UpdateUserProfileRequest,
    ) {
        user.username = request.username
        user.birth = request.birth
        user.gender = request.gender
        user.startupStatus = request.startupStatus
        user.companyName = request.companyName
        user.companyDescription = request.companyDescription
        user.numberOfEmployees = request.numberOfEmployees
        user.companyWebsite = request.companyWebsite
        user.startupLocation = request.startupLocation
        user.annualRevenue = request.annualRevenue

        userRepository.save(user)
        userCache.put(user)

        request.startupFields?.let { newInterests ->
            userStartupFieldRepository.deleteByUser(user)
            val userStartupFields =
                newInterests.map { startupField ->
                    UserStartupField(
                        user = user,
                        businessType = startupField.businessType,
                        customField = startupField.customField,
                    )
                }
            userStartupFieldRepository.saveAll(userStartupFields)
        }
    }

    @Transactional(readOnly = true)
    fun getUser(user: User): UserResponse {
        val startupFields =
            userStartupFieldRepository.findByUser(user).map {
                StartupFieldResponse(
                    businessType = it.businessType,
                    customField = it.customField,
                )
            }
        return UserResponse(
            id = user.id!!,
            email = user.email,
            username = user.username,
            birth = user.birth,
            gender = user.gender,
            startupStatus = user.startupStatus,
            companyName = user.companyName,
            companyDescription = user.companyDescription,
            numberOfEmployees = user.numberOfEmployees,
            companyWebsite = user.companyWebsite,
            startupLocation = user.startupLocation,
            annualRevenue = user.annualRevenue,
            startupFields = startupFields,
            startupHistory = user.startupHistory,
            provider = user.provider,
        )
    }

    fun signOut(user: User) {
        tokenRedisService.deleteRefreshToken(user.email)
    }

    fun deleteAccount(
        user: User,
        request: DeleteUserRequest,
    ) {
        if (user.provider == AuthType.LOCAL) {
            if (request.password.isNullOrBlank()) {
                throw InvalidPasswordException("비밀번호를 입력해주세요.")
            }
            if (user.password == null || !passwordEncoder.matches(request.password, user.password)) {
                throw InvalidPasswordException("잘못된 비밀번호")
            }
        }

        user.deleted = true
        user.deletedAt = LocalDateTime.now()
        userRepository.save(user)

        tokenRedisService.deleteRefreshToken(user.email)
        user.id?.let { userCache.evict(it) }
    }

    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException("찾을 수 없는 유저")
        val companies = companyRepository.findByFounderAndDeletedFalse(user)
        val startupFields =
            userStartupFieldRepository.findByUser(user).map {
                StartupFieldResponse(
                    businessType = it.businessType,
                    customField = it.customField,
                )
            }
        return UserProfileResponse(
            username = user.username,
            profileImage = user.profileImage,
            companyIds = companies.mapNotNull { it.id },
            birth = user.birth,
            gender = user.gender,
            startupStatus = user.startupStatus,
            companyName = user.companyName,
            companyDescription = user.companyDescription,
            numberOfEmployees = user.numberOfEmployees,
            companyWebsite = user.companyWebsite,
            startupLocation = user.startupLocation,
            annualRevenue = user.annualRevenue,
            startupFields = startupFields,
            startupHistory = user.startupHistory,
        )
    }
}
