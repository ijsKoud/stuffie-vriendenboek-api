package nl.klrnbk.security.idp.exceptions

import nl.klrnbk.common.exceptions.ApiException
import org.springframework.http.HttpStatus

class InvalidJwtException(
    instance: String? = null,
) : ApiException(
        HttpStatus.UNAUTHORIZED,
        "Error while setting up security context",
        "Supplied JWT is either invalid or expired",
        instance,
    )
