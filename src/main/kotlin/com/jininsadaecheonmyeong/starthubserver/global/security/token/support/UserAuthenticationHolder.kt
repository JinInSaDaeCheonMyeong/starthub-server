package com.jininsadaecheonmyeong.starthubserver.global.security.token.support

import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.InvalidTokenException
import com.jininsadaecheonmyeong.starthubserver.domain.user.exception.UserNotFoundException
import com.jininsadaecheonmyeong.starthubserver.domain.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class UserAuthenticationHolder(
    private val userRepository: UserRepository,
) {
    fun current(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is String) {
            throw InvalidTokenException("인증되지 않은 사용자입니다.")
        }
        return userRepository.findById(principal.toLong())
            .orElseThrow { UserNotFoundException("찾을 수 없는 유저") }
    }
}
