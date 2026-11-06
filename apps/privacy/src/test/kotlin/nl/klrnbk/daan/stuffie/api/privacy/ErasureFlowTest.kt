package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.common.Platforms.PHOTOS
import nl.klrnbk.common.Platforms.STUFFIES
import nl.klrnbk.daan.stuffie.api.privacy.api.service.PlatformDataDeletionService
import nl.klrnbk.daan.stuffie.api.privacy.api.service.TombstoneRegistryService
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ErasureFlowTest : BaseTest() {
    @Autowired lateinit var deletion: PlatformDataDeletionService

    @Autowired lateinit var tombstones: TombstoneRegistryService

    @Test
    fun `asks every platform to erase the user`() {
        // Arrange
        val tombstone = tombstones.createOrUpdateDeleteRequest(newHash())

        // Act
        val complete = deletion.erase(listOf(tombstone))

        // Assert
        assertTrue(complete)
        assertEquals(setOf(STUFFIES, PHOTOS), platformClient.platformsAsked())
    }

    @Test
    fun `does not ask a platform again once it confirmed`() {
        // Arrange
        val tombstone = tombstones.createOrUpdateDeleteRequest(newHash())
        deletion.erase(listOf(tombstone))
        platformClient.forgetRequests()

        // Act
        val complete = deletion.erase(listOf(tombstone))

        // Assert
        assertTrue(complete)
        assertEquals(0, platformClient.requestCount())
    }

    @Test
    fun `reports incomplete when a platform is down`() {
        // Arrange
        val tombstone = tombstones.createOrUpdateDeleteRequest(newHash())
        platformClient.takeDown(PHOTOS)

        // Act
        val complete = deletion.erase(listOf(tombstone))

        // Assert
        assertFalse(complete)
    }

    @Test
    fun `retries only the platform that failed`() {
        // Arrange
        val tombstone = tombstones.createOrUpdateDeleteRequest(newHash())
        platformClient.takeDown(PHOTOS)
        deletion.erase(listOf(tombstone))
        platformClient.bringUp()
        platformClient.forgetRequests()

        // Act
        val complete = deletion.erase(listOf(tombstone))

        // Assert
        assertTrue(complete)
        assertEquals(setOf(PHOTOS), platformClient.platformsAsked())
    }

    @Test
    fun `force asks confirmed platforms again, as needed after a restored backup`() {
        // Arrange
        val tombstone = tombstones.createOrUpdateDeleteRequest(newHash())
        deletion.erase(listOf(tombstone))
        platformClient.forgetRequests()

        // Act
        deletion.erase(listOf(tombstone), force = true)

        // Assert
        assertEquals(setOf(STUFFIES, PHOTOS), platformClient.platformsAsked())
    }

    @Test
    fun `a new request for the same user erases again and keeps a single tombstone`() {
        // Arrange
        val hash = newHash()
        val first = tombstones.createOrUpdateDeleteRequest(hash, Instant.parse("2026-01-01T00:00:00Z"))
        deletion.erase(listOf(first))
        platformClient.forgetRequests()

        // Act
        val second = tombstones.createOrUpdateDeleteRequest(hash, Instant.parse("2026-02-01T00:00:00Z"))
        deletion.erase(listOf(second))

        // Assert
        assertEquals(first.id, second.id)
        assertEquals(1, tombstones.findAll().count { it.subHash == hash })
        assertEquals(setOf(STUFFIES, PHOTOS), platformClient.platformsAsked())
    }
}
