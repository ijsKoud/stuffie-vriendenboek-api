package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.common.Platforms.STUFFIES
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import nl.klrnbk.daan.stuffie.api.privacy.config.PrivacyProperties
import nl.klrnbk.daan.stuffie.api.privacy.mocks.PlatformMock
import nl.klrnbk.daan.stuffie.api.privacy.providers.clients.internalservices.PlatformClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlatformClientTest {
    private val platform = PlatformMock()

    private fun clientFor(platform: PlatformMock) =
        PlatformClient(
            ApplicationProperties(
                PrivacyProperties(
                    subHashSalt = "salt",
                    internalApiKey = "secret",
                    platforms = mapOf(STUFFIES to platform.url),
                    scheduledApiKey = "scheduled",
                ),
            ),
        )

    @AfterTest
    fun stopPlatform() = platform.stop()

    @Test
    fun `posts the hashes as json to the erasure endpoint`() {
        // Arrange
        val client = clientFor(platform)

        // Act
        client.erase(STUFFIES, listOf("a", "b"))

        // Assert
        assertEquals("POST", platform.method)
        assertEquals("/v1/internal/erasures", platform.path)
        assertEquals("""{"subHashes":["a","b"]}""", platform.body)
    }

    @Test
    fun `authenticates with the internal key`() {
        // Arrange
        val client = clientFor(platform)

        // Act
        client.erase(STUFFIES, listOf("a"))

        // Assert
        assertEquals("secret", platform.internalKey)
    }

    @Test
    fun `throws when the platform answers an error`() {
        // Arrange
        platform.status = 500
        val client = clientFor(platform)

        // Act + Assert
        assertFailsWith<Exception> { client.erase(STUFFIES, listOf("a")) }
    }

    @Test
    fun `knows the configured platforms`() {
        // Act
        val client = clientFor(platform)

        // Assert
        assertEquals(setOf(STUFFIES), client.platforms)
    }
}
