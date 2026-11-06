package nl.klrnbk.daan.stuffie.api.privacy.mocks

import nl.klrnbk.common.Platforms
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import nl.klrnbk.daan.stuffie.api.privacy.providers.clients.internalservices.PlatformClient

/** Records the erasure requests instead of doing http; a platform marked down throws. */
class MockPlatformClient(
    properties: ApplicationProperties,
) : PlatformClient(properties) {
    private val requests = mutableListOf<Pair<Platforms, List<String>>>()
    private val down = mutableSetOf<Platforms>()

    override fun erase(
        platform: Platforms,
        subHashes: List<String>,
    ) {
        check(platform !in down) { "$platform is down" }
        requests += platform to subHashes
    }

    fun takeDown(platform: Platforms) = down.add(platform)

    fun bringUp() = down.clear()

    fun hashesSentTo(platform: Platforms): List<String> = requests.filter { it.first == platform }.flatMap { it.second }

    fun platformsAsked(): Set<Platforms> = requests.map { it.first }.toSet()

    fun requestCount() = requests.size

    fun forgetRequests() = requests.clear()
}
