package com.jininsadaecheonmyeong.starthubserver.global.security.token.exception

class ExpiredTokenException(message: String = "토큰 만료됨") : RuntimeException(message)