package nl.klrnbk.daan.stuffie.api.privacy.config

import nl.klrnbk.common.Platforms
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stuffie")
data class ApplicationProperties(
    val privacy: PrivacyProperties,
)

data class PrivacyProperties(
    /** Random, and never change it: it is baked into photo folder names and erasure tombstones. */
    val subHashSalt: String,
    /** Shared secret the other services expect in `X-Internal-Key` on their erasure endpoint. */
    val internalApiKey: String,
    /** Base url of every service that holds user data. */
    val platforms: Map<Platforms, String>,
    /** Secret a scheduler (e.g. a k8s CronJob) sends in `X-Api-Key` to trigger the jobs under /v1/scheduled. */
    val scheduledApiKey: String,
) {
    init {
        require(scheduledApiKey.isNotBlank()) { "stuffie.privacy.scheduled-api-key is blank: anyone could trigger the jobs" }
        require(platforms.isNotEmpty()) { "stuffie.privacy.platforms is empty: no data would ever be erased" }
    }
}
