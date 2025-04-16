package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.domain.user.enumeration.UserRole
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.data.UserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service.GoogleService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OAuth2Service(
    private val tokenProvider: TokenProvider,
    private val googleService: GoogleService,
    private val userRepository: UserRepository
) {

    fun auth(request: String): TokenResponse {
        val userInfo: UserInfo = googleService.exchangeCodeForUserInfo(request)

        if (!userRepository.existsByEmail(userInfo.email)) {
            val user = User(
                email = userInfo.email,
                role = UserRole.USER,
                provider = AuthProvider.GOOGLE,
                providerId = userInfo.sub
            )
            userRepository.save(user)
            return TokenResponse(
                access = tokenProvider.generateAccess(user),
                refresh = tokenProvider.generateRefresh(user)
            )
        }

        val user = userRepository.findByEmail(userInfo.email) ?: throw UserNotFoundException("유저를 찾을 수 없음")
        return TokenResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user)
        )
    }
}