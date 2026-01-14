package com.jininsadaecheonmyeong.starthubserver.exception.announcement

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class LikeAlreadyExistsException(message: String) : CustomException(message, HttpStatus.CONFLICT)
