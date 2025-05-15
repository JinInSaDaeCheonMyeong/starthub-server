package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.data.TokenResponse
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserRole
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service.AppleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthUserInfo
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.google.service.GoogleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.naver.service.NaverService
import com.jininsadaecheonmyeong.starthubserver.global.security.token.core.TokenProvider
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    private val tokenProvider: TokenProvider,
    private val googleService: GoogleService,
    private val naverService: NaverService,
    private val appleService: AppleService,
    private val userRepository: UserRepository
) {
    fun googleAuth(code: String, authProvider: AuthProvider): TokenResponse {
        val userInfo = googleService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, authProvider)
    }

    fun naverAuth(code: String, state: String, authProvider: AuthProvider): TokenResponse {
        val userInfo = naverService.exchangeCodeForUserInfo(code, state)
        return processOAuthLogin(userInfo, authProvider)
    }

    fun appleAuth(code: String, authProvider: AuthProvider): TokenResponse {
        val userInfo = appleService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, authProvider)
    }

    private fun processOAuthLogin(info: OAuthUserInfo, provider: AuthProvider): TokenResponse {
        if (!userRepository.existsByEmail(info.email)) {
            val user = User(
                email = info.email,
                role = UserRole.USER,
                provider = provider,
                providerId = info.id
            )
            userRepository.save(user)
            return TokenResponse(
                tokenProvider.generateAccess(user),
                tokenProvider.generateRefresh(user)
            )
        }

        val user = userRepository.findByEmail(info.email) ?: throw UserNotFoundException("유저를 찾을 수 없음")
        return TokenResponse(
            tokenProvider.generateAccess(user),
            tokenProvider.generateRefresh(user)
        )
    }
}