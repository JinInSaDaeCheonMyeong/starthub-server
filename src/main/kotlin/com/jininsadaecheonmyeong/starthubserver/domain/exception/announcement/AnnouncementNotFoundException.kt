package com.jininsadaecheonmyeong.starthubserver.domain.exception.announcement

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class AnnouncementNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
