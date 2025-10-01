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
    message: String = "Search API quota exceeded",
    cause: Throwable? = null,
) : SearchException(message, cause)

class InvalidSearchParametersException(
    message: String = "Invalid search parameters provided",
    cause: Throwable? = null,
) : SearchException(message, cause)
