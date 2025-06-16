package com.jininsadaecheonmyeong.starthubserver.domain.chat.exception

import com.jininsadaecheonmyeong.starthubserver.global.exception.CustomException
import org.springframework.http.HttpStatus

class ChatRoomNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)