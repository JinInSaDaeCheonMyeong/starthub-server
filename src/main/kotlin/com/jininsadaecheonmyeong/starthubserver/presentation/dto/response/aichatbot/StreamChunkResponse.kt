package com.jininsadaecheonmyeong.starthubserver.presentation.dto.response.aichatbot

import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.Reference
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamChunk
import com.jininsadaecheonmyeong.starthubserver.application.service.aichatbot.StreamEventType

data class StreamChunkResponse(
    val type: String,
    val text: String?,
    val done: Boolean,
    val references: List<ReferenceResponse>? = null,
) {
    companion object {
        fun from(chunk: StreamChunk): StreamChunkResponse {
            return StreamChunkResponse(
                type = chunk.type.name,
                text = chunk.text,
                done = chunk.type == StreamEventType.MESSAGE_STOP,
                references = chunk.references?.map { ReferenceResponse.from(it) },
            )
        }
    }
}

data class ReferenceResponse(
    val type: String,
    val id: Long,
    val title: String,
    val url: String?,
) {
    companion object {
        fun from(reference: Reference): ReferenceResponse {
            return ReferenceResponse(
                type = reference.type.name,
                id = reference.id,
                title = reference.title,
                url = reference.url,
            )
        }
    }
}
