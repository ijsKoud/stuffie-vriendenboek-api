package nl.klrnbk.common.exceptions

import org.springframework.http.HttpStatus

open class NotFoundException(
    detail: String,
    instance: String? = null,
) : ApiException(
        HttpStatus.NOT_FOUND,
        "Resource not found.",
        detail,
        instance,
    )
