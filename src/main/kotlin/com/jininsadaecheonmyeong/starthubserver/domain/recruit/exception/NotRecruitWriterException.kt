
package com.jininsadaecheonmyeong.starthubserver.domain.recruit.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class NotRecruitWriterException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)
