package com.jininsadaecheonmyeong.starthubserver.domain.user.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.RefreshRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.data.UserRequest
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.EmailAlreadyExistsException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidPasswordException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenParser
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenValidator
import com.jininsadaecheonmyeong.starthubserver.global.security.token.enumeration.TokenType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService (
    private val userRepository: UserRepository,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val tokenProvider: TokenProvider,
    private val tokenValidator: TokenValidator,
    private val tokenParser: TokenParser
) {
    fun signUp(request: UserRequest) {
        if (userRepository.existsByEmail(request.email)) throw EmailAlreadyExistsException("이미 등록된 이메일")
        userRepository.save(request.toEntity(bcryptPasswordEncoder.encode(request.password)))
    }

    fun signIn(request: UserRequest): TokenResponse {
        val user: User = userRepository.findByEmail(request.email) ?: throw UserNotFoundException("찾을 수 없는 유저")
        if (!bcryptPasswordEncoder.matches(request.password, user.password)) throw InvalidPasswordException("잘못된 비밀번호")
        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user)
        )
    }

    fun reissue(request: RefreshRequest): TokenResponse {
        tokenValidator.validateAll(request.refresh, TokenType.REFRESH_TOKEN)
        val user: User = userRepository.findByEmail(tokenParser.findEmail(request.refresh))
            ?: throw UserNotFoundException("찾을 수 없는 유저")
        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user)
        )
    }
}