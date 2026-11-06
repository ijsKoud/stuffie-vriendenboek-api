package nl.klrnbk.common.exceptions

import org.springframework.http.HttpStatus

open class BadRequestException(
    detail: String,
    instance: String? = null,
) : ApiException(
        HttpStatus.BAD_REQUEST,
        "Client sent a bad request.",
        detail,
        instance,
    )
