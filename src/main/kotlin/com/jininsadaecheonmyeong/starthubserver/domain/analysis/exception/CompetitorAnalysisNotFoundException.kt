package com.jininsadaecheonmyeong.starthubserver.domain.analysis.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class CompetitorAnalysisNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)
