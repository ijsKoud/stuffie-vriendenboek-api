package nl.klrnbk.security.idp.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stuffie.spring.security")
data class SecurityConfigurationProperties(
    val unauthenticatedRoutes: List<String> = listOf(),
    val authorization: AuthorizationProperties,
)

data class AuthorizationProperties(
    /** Must equal the `iss` claim of the access tokens (Authentik: includes the trailing slash). */
    val issuer: String,
    /** Must equal the `aud` claim of the access tokens (the OIDC client id). */
    val audience: String,
    val jwks: JwksProperties,
)

data class JwksProperties(
    val uri: String,
    val refreshInterval: String,
)
