package nl.klrnbk.security.idp.client.openid.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenIdJwksResponse(
    val keys: List<OpenIdJwksResponseKeys> = listOf(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenIdJwksResponseKeys(
    val alg: String? = null,
    val kid: String? = null,
    val kty: String? = null,
    val use: String? = null,
    val n: String? = null,
    val e: String? = null,
)
