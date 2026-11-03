package nl.klrnbk.daan.stuffie.api.privacy.providers.clients.internalservices

import nl.klrnbk.common.Platforms
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

/**
 * Contract every platform implements: `POST /v1/internal/erasures` with `{"subHashes": [...]}` and the `X-Internal-Key`
 * header, answering 2xx once it holds no data of those subjects anymore. Must be idempotent.
 */
@Component
class PlatformClient(
    properties: ApplicationProperties,
) {
    private val apiKey = properties.privacy.internalApiKey
    private val clients =
        properties.privacy.platforms.mapValues { (_, url) ->
            val factory =
                JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build())
                    .apply { setReadTimeout(Duration.ofSeconds(30)) }
            RestClient.builder().baseUrl(url).requestFactory(factory).build()
        }

    val platforms: Set<Platforms> get() = clients.keys

    /** Throws when the platform is unreachable or does not answer 2xx. */
    fun erase(
        platform: Platforms,
        subHashes: List<String>,
    ) {
        clients
            .getValue(platform)
            .post()
            .uri("/v1/internal/erasures")
            .header("X-Internal-Key", apiKey)
            .body(mapOf("subHashes" to subHashes))
            .retrieve()
            .toBodilessEntity()
    }
}
