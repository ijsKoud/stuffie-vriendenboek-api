package nl.klrnbk.security.idp.exceptions

import nl.klrnbk.common.exceptions.ApiException
import org.springframework.http.HttpStatus

class JwtVerifyException(
    detail: String,
    instance: String? = null,
) : ApiException(
        HttpStatus.UNAUTHORIZED,
        "Error while setting up security context",
        detail,
        instance,
    )
