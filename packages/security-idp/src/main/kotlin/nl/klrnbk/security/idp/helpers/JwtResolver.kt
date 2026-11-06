package nl.klrnbk.security.idp.helpers

import jakarta.servlet.http.HttpServletRequest
import nl.klrnbk.security.idp.constants.TOKEN_HEADER_NAME
import nl.klrnbk.security.idp.constants.TOKEN_HEADER_VALUE_PREFIX

fun resolveJwtFromRequest(request: HttpServletRequest): String? {
    val rawHeader: String? = request.getHeader(TOKEN_HEADER_NAME)
    if (rawHeader == null) return null

    if (rawHeader.startsWith(TOKEN_HEADER_VALUE_PREFIX, ignoreCase = true)) {
        return rawHeader.substring(TOKEN_HEADER_VALUE_PREFIX.length)
    }

    return rawHeader
}
