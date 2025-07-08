package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.email.exception.EmailNotVerifiedException
import com.jininsadaecheonmyeong.starthubserver.domain.email.repository.EmailRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.UserInterest
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.BusinessType
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserInterestRepository
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenRedisService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enums.TokenType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
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
        )
    }

    @Transactional
    fun updateUserProfile(
        user: User,
        username: String,
        interests: List<BusinessType>,
        profileImage: String,
    ) {
        user.username = username
        user.profileImage = profileImage
        userRepository.save(user)

        userInterestRepository.deleteByUser(user)

        val newInterests =
            interests.map { interestType ->
                UserInterest(user = user, businessType = interestType)
            }
        userInterestRepository.saveAll(newInterests)
    }
}
