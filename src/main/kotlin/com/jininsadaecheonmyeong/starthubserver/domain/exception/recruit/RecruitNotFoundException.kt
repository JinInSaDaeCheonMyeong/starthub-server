package com.jininsadaecheonmyeong.starthubserver.domain.exception.recruit

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class RecruitNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
