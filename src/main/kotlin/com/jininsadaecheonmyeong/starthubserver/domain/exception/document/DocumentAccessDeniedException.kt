package com.jininsadaecheonmyeong.starthubserver.domain.exception.document

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentAccessDeniedException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)