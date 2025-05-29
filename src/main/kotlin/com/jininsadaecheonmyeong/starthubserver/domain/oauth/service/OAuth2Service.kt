package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthType
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.apple.service.AppleService
import com.jininsadaecheonmyeong.starthubserver.global.infra.oauth.common.OAuthResponse
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
    fun googleAuth(code: String): OAuthResponse {
        val userInfo = googleService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, AuthType.GOOGLE)
    }

    fun naverAuth(code: String): OAuthResponse {
        val userInfo = naverService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, AuthType.NAVER)
    }

    fun appleAuth(code: String): OAuthResponse {
        val userInfo = appleService.exchangeCodeForUserInfo(code)
        return processOAuthLogin(userInfo, AuthType.APPLE)
    }

    private fun processOAuthLogin(info: OAuthUserInfo, provider: AuthType): OAuthResponse {
        val existingUser = userRepository.findByEmail(info.email)
        val isFirstLogin = existingUser == null
        val user = existingUser ?: userRepository.save(info.toUser(provider))
        return provideTokens(user, isFirstLogin)
    }

    private fun provideTokens(user: User, isFirstLogin: Boolean) =
        OAuthResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
            isFirstLogin = isFirstLogin
        )
}
