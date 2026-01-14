package com.jininsadaecheonmyeong.starthubserver.exception.recruit

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class NotRecruitWriterException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)
