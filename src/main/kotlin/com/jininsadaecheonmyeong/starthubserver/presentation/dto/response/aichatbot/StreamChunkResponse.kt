package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamChunk
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamEventType

data class StreamChunkResponse(
    val type: String,
    val text: String?,
    val done: Boolean,
) {
    companion object {
        fun from(chunk: StreamChunk): StreamChunkResponse {
            return StreamChunkResponse(
                type = chunk.type.name,
                text = chunk.text,
                done = chunk.type == StreamEventType.MESSAGE_STOP,
            )
        }
    }
}
