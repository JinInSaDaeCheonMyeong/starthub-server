package com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class LikeNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
