package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.common.Platforms.PHOTOS
import nl.klrnbk.common.Platforms.STUFFIES
import nl.klrnbk.daan.stuffie.api.privacy.api.service.TombstoneRegistryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@AutoConfigureMockMvc
class ScheduledTasksApiTest : BaseTest() {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var tombstones: TombstoneRegistryService

    private fun triggerErasureReplay(apiKey: String? = null) =
        mockMvc.post("/v1/scheduled/erasure") { apiKey?.let { header("x-api-key", it) } }

    @Test
    fun `a valid api key replays every erasure to every platform`() {
        // Arrange
        val hash = newHash()
        tombstones.createOrUpdateDeleteRequest(hash)
        platformClient.forgetRequests()

        // Act
        val response = triggerErasureReplay(SCHEDULED_API_KEY)

        // Assert
        response.andExpect { status { isNoContent() } }
        assertTrue(hash in platformClient.hashesSentTo(STUFFIES))
        assertTrue(hash in platformClient.hashesSentTo(PHOTOS))
    }

    @Test
    fun `replays again even for platforms that confirmed before`() {
        // Arrange
        val hash = newHash()
        tombstones.createOrUpdateDeleteRequest(hash)
        triggerErasureReplay(SCHEDULED_API_KEY)
        platformClient.forgetRequests()

        // Act
        triggerErasureReplay(SCHEDULED_API_KEY)

        // Assert
        assertTrue(hash in platformClient.hashesSentTo(STUFFIES))
    }

    @Test
    fun `answers 401 without an api key and replays nothing`() {
        // Arrange
        tombstones.createOrUpdateDeleteRequest(newHash())
        platformClient.forgetRequests()

        // Act
        val response = triggerErasureReplay(apiKey = null)

        // Assert
        response.andExpect { status { isUnauthorized() } }
        assertEquals(0, platformClient.requestCount())
    }

    @Test
    fun `answers 401 with a wrong api key and replays nothing`() {
        // Arrange
        tombstones.createOrUpdateDeleteRequest(newHash())
        platformClient.forgetRequests()

        // Act
        val response = triggerErasureReplay("not-the-key")

        // Assert
        response.andExpect { status { isUnauthorized() } }
        assertEquals(0, platformClient.requestCount())
    }

    @Test
    fun `a user token is not an api key`() {
        // Act
        val response = mockMvc.post("/v1/scheduled/erasure") { header("X-Authorization", "Bearer ${token(newSub())}") }

        // Assert
        response.andExpect { status { isUnauthorized() } }
    }
}
