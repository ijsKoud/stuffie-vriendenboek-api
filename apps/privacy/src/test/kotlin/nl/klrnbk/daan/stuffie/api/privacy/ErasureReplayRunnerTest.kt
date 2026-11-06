package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.common.Platforms.PHOTOS
import nl.klrnbk.common.Platforms.STUFFIES
import nl.klrnbk.daan.stuffie.api.privacy.api.service.ErasureReplayRunnerService
import nl.klrnbk.daan.stuffie.api.privacy.api.service.PlatformDataDeletionService
import nl.klrnbk.daan.stuffie.api.privacy.api.service.TombstoneRegistryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.DefaultApplicationArguments
import kotlin.test.Test
import kotlin.test.assertTrue

class ErasureReplayRunnerTest : BaseTest() {
    @Autowired lateinit var runner: ErasureReplayRunnerService

    @Autowired lateinit var deletion: PlatformDataDeletionService

    @Autowired lateinit var tombstones: TombstoneRegistryService

    @Test
    fun `startup sends every tombstone to every platform again`() {
        // Arrange
        val hash = newHash()
        deletion.erase(listOf(tombstones.createOrUpdateDeleteRequest(hash)))
        platformClient.forgetRequests()

        // Act
        runner.run(DefaultApplicationArguments())

        // Assert
        assertTrue(hash in platformClient.hashesSentTo(STUFFIES))
        assertTrue(hash in platformClient.hashesSentTo(PHOTOS))
    }
}
