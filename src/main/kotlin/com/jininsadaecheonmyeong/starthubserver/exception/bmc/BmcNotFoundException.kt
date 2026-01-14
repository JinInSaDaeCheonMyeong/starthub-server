package com.jininsadaecheonmyeong.starthubserver.exception.bmc

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class BmcNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
