package com.jininsadaecheonmyeong.starthubserver.global.infra.search.exception

open class SearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class CompanyInfoExtractException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class SearchQuotaExceededException(
    message: String = "Google Search JSON API 사용량 초과",
    cause: Throwable? = null,
) : SearchException(message, cause)

class InvalidSearchParametersException(
    message: String = "잘못된 검색 파라미터",
    cause: Throwable? = null,
) : SearchException(message, cause)
