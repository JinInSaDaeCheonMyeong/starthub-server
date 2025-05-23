package com.jininsadaecheonmyeong.starthubserver.domain.oauth.service

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.AuthProvider
import com.jininsadaecheonmyeong.starthubserver.domain.user.enums.UserRole
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
    fun googleAuth(code: String): OAuthResponse =
        processOAuthLogin(googleService.exchangeCodeForUserInfo(code), AuthProvider.GOOGLE)

    fun naverAuth(code: String): OAuthResponse =
        processOAuthLogin(naverService.exchangeCodeForUserInfo(code), AuthProvider.NAVER)

    fun appleAuth(code: String): OAuthResponse =
        processOAuthLogin(appleService.exchangeCodeForUserInfo(code), AuthProvider.APPLE)

    private fun processOAuthLogin(info: OAuthUserInfo, provider: AuthProvider): OAuthResponse {
        val user = userRepository.findByEmail(info.email)

        return if (user == null) {
            val newUser = userRepository.save(
                User(
                    email = info.email,
                    role = UserRole.USER,
                    provider = provider,
                    providerId = info.sub
                )
            )
            generateOAuthResponse(newUser, true)
        } else {
            generateOAuthResponse(user, false)
        }
    }

    private fun generateOAuthResponse(user: User, isFirstLogin: Boolean): OAuthResponse {
        return OAuthResponse(
            access = tokenProvider.generateAccess(user),
            refresh = tokenProvider.generateRefresh(user),
            isFirstLogin = isFirstLogin
        )
    }
}