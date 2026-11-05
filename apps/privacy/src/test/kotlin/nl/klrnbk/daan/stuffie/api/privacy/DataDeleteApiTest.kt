package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.common.Platforms.PHOTOS
import nl.klrnbk.common.Platforms.STUFFIES
import nl.klrnbk.common.helpers.hash
import nl.klrnbk.daan.stuffie.api.privacy.api.service.TombstoneRegistryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@AutoConfigureMockMvc
class DataDeleteApiTest : BaseTest() {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var tombstones: TombstoneRegistryService

    private fun deleteAllDataOf(sub: String) = mockMvc.delete("/v1/delete") { header("X-Authorization", "Bearer ${token(sub)}") }

    @Test
    fun `answers 204 and sends the hashed user to every platform`() {
        // Arrange
        val sub = newSub()
        val hashedSub = hash(sub, SALT)

        // Act
        val response = deleteAllDataOf(sub)

        // Assert
        response.andExpect { status { isNoContent() } }
        assertEquals(listOf(hashedSub), platformClient.hashesSentTo(STUFFIES))
        assertEquals(listOf(hashedSub), platformClient.hashesSentTo(PHOTOS))
    }

    @Test
    fun `keeps only the hash of the user, never the sub`() {
        // Arrange
        val sub = newSub()

        // Act
        deleteAllDataOf(sub)

        // Assert
        val stored = tombstones.findAll().map { it.subHash }
        assertTrue(hash(sub, SALT) in stored)
        assertTrue(sub !in stored)
    }

    @Test
    fun `answers 202 and keeps the request when a platform is down`() {
        // Arrange
        val sub = newSub()
        platformClient.takeDown(PHOTOS)

        // Act
        val response = deleteAllDataOf(sub)

        // Assert
        response.andExpect { status { isAccepted() } }
        assertTrue(hash(sub, SALT) in tombstones.findAll().map { it.subHash })
    }

    @Test
    fun `answers 401 without a token and erases nothing`() {
        // Act
        val response = mockMvc.delete("/v1/delete")

        // Assert
        response.andExpect { status { isUnauthorized() } }
        assertEquals(0, platformClient.requestCount())
    }
}
