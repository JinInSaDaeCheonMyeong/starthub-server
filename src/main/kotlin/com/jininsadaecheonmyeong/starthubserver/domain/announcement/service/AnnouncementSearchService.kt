package com.jininsadaecheonmyeong.starthubserver.domain.announcement.service

import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.request.NaturalLanguageSearchRequest
import com.jininsadaecheonmyeong.starthubserver.domain.announcement.data.response.NaturalLanguageSearchResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class AnnouncementSearchService(
    private val webClient: WebClient,
    @param:Value("\${SEARCH_SERVER_URL}") private val searchServerUrl: String,
) {
    fun searchAnnouncement(request: NaturalLanguageSearchRequest): Mono<NaturalLanguageSearchResponse> {
        return webClient.post()
            .uri(searchServerUrl)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<NaturalLanguageSearchResponse>()
    }
}
