package nl.klrnbk.security.idp.client.openid

import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponse
import nl.klrnbk.security.idp.config.SecurityConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
@EnableConfigurationProperties(SecurityConfigurationProperties::class)
class OpenIdClient(
    val properties: SecurityConfigurationProperties,
) {
    private val apiClient = RestClient.create()

    fun getFreshJwks(): OpenIdJwksResponse =
        apiClient
            .get()
            .uri(properties.authorization.jwks.uri)
            .retrieve()
            .body<OpenIdJwksResponse>() ?: OpenIdJwksResponse()
}
