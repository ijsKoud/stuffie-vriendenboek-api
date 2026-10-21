package nl.klrnbk.security.idp.models

import com.auth0.jwt.interfaces.DecodedJWT
import nl.klrnbk.security.idp.service.OpenIdService
import org.springframework.security.authentication.AbstractAuthenticationToken

class JwtAuthenticationToken(
    val decodedJWT: DecodedJWT,
    val openIdService: OpenIdService,
) : AbstractAuthenticationToken(listOf()) {
    override fun getCredentials(): String = decodedJWT.token

    /** The OIDC `sub` claim: the owner key of all user data. */
    override fun getPrincipal(): String = decodedJWT.subject

    override fun isAuthenticated(): Boolean = openIdService.verifyJwt(credentials)
}
