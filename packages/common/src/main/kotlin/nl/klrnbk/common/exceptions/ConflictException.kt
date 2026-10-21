package nl.klrnbk.common.exceptions

import org.springframework.http.HttpStatus

open class ConflictException(
    detail: String,
    instance: String? = null,
) : ApiException(
        HttpStatus.CONFLICT,
        "The resource is not in a state that allows this.",
        detail,
        instance,
    )
