package com.jininsadaecheonmyeong.starthubserver.domain.announcement.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class LikeAlreadyExistsException(message: String) : CustomException(message, HttpStatus.CONFLICT)
