package nl.klrnbk.stuffie

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import nl.klrnbk.security.idp.client.openid.OpenIdClient
import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponse
import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponseKeys
import nl.klrnbk.security.idp.config.SecurityConfigurationProperties
import nl.klrnbk.stuffie.api.service.ErasureService
import nl.klrnbk.stuffie.datasource.models.StuffieModel
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import nl.klrnbk.stuffie.datasource.repositories.ErasureTombstoneRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieRepository
import nl.klrnbk.stuffie.helpers.SubHasher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val ISSUER = "https://idp.test/application/o/stuffie/"
private const val AUDIENCE = "stuffie-client"
private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

private fun unsigned(value: java.math.BigInteger): String {
    val bytes = value.toByteArray().let { if (it[0] == 0.toByte()) it.copyOfRange(1, it.size) else it }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

@TestConfiguration
class TestJwksConfiguration {
    /** Serves the test key instead of fetching a JWKS from an IdP. */
    @Bean
    @Primary
    fun testOpenIdClient(properties: SecurityConfigurationProperties): OpenIdClient {
        val public = keyPair.public as RSAPublicKey
        val jwks = OpenIdJwksResponse(listOf(OpenIdJwksResponseKeys("RS256", "test-key", "RSA", "sig", unsigned(public.modulus), unsigned(public.publicExponent))))

        return object : OpenIdClient(properties) {
            override fun getFreshJwks() = jwks
        }
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwksConfiguration::class)
class StuffieApiTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var stuffieRepository: StuffieRepository

    @Autowired lateinit var tombstones: ErasureTombstoneRepository

    @Autowired lateinit var erasureService: ErasureService

    @Autowired lateinit var subHasher: SubHasher

    private fun token(
        sub: String,
        audience: String = AUDIENCE,
    ): String =
        JWT
            .create()
            .withKeyId("test-key")
            .withIssuer(ISSUER)
            .withAudience(audience)
            .withSubject(sub)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
            .sign(Algorithm.RSA256(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey))

    private fun newUser() = "user-${UUID.randomUUID()}"

    private fun auth(sub: String) = "Bearer ${token(sub)}"

    private fun create(
        sub: String,
        body: String,
    ): String {
        val result =
            mockMvc
                .post("/v1/stuffies") {
                    header("X-Authorization", auth(sub))
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect { status { isCreated() } }
                .andReturn()

        return "\"id\":\"([^\"]+)\"".toRegex().find(result.response.contentAsString)!!.groupValues[1]
    }

    private fun png(
        width: Int,
        height: Int,
    ): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", out)
        return out.toByteArray()
    }

    private fun upload(
        sub: String,
        content: ByteArray,
        name: String = "x.png",
    ): MvcResult =
        mockMvc
            .multipart("/v1/photos") {
                file(MockMultipartFile("file", name, "image/png", content))
                header("X-Authorization", auth(sub))
            }.andReturn()

    private fun uploadedId(result: MvcResult) = "\"id\":\"([^\"]+)\"".toRegex().find(result.response.contentAsString)!!.groupValues[1]

    @Test
    fun `requires a valid token for the protected routes`() {
        mockMvc.get("/v1/stuffies").andExpect { status { isUnauthorized() } }
        mockMvc
            .get("/v1/stuffies") { header("X-Authorization", "Bearer ${token(newUser(), audience = "someone-else")}") }
            .andExpect { status { isUnauthorized() } }
        mockMvc
            .get("/v1/stuffies") { header("X-Authorization", "Bearer garbage") }
            .andExpect { status { isUnauthorized() } }
        mockMvc
            .get("/v1/stuffies") { header("X-Authorization", auth(newUser())) }
            .andExpect {
                status { isOk() }
                content { json("[]") }
            }
    }

    @Test
    fun `serves the api docs and health without a token`() {
        mockMvc.get("/v3/api-docs").andExpect {
            status { isOk() }
            jsonPath("$.paths['/v1/stuffies']") { exists() }
        }
        mockMvc.get("/actuator/health/liveness").andExpect { status { isOk() } }
    }

    @Test
    fun `validates input and answers errors in the problem shape`() {
        mockMvc
            .post("/v1/stuffies") {
                header("X-Authorization", auth(newUser()))
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "  "}"""
            }.andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith("application/problem+json") }
                jsonPath("$.status") { value(400) }
                jsonPath("$.instance") { value("/v1/stuffies") }
            }
    }

    @Test
    fun `stuffie lifecycle with the rainbow bridge and sharing`() {
        val sub = newUser()
        val id = create(sub, """{"name": " Mr. Bunny ", "species": "", "hobbies": "naps", "birthday": "2009-12-24"}""")

        mockMvc.get("/v1/stuffies/$id") { header("X-Authorization", auth(sub)) }.andExpect {
            jsonPath("$.name") { value("Mr. Bunny") }
            jsonPath("$.species") { value(null) }
            jsonPath("$.hobbies") { value("naps") }
            jsonPath("$.status") { value("ACTIVE") }
        }

        // goodbye with a note; a second goodbye conflicts
        val bridge = """{"leftAt": "2026-03-01", "farewellMessage": "Sleep tight"}"""
        mockMvc
            .post("/v1/stuffies/$id/rainbow-bridge") {
                header("X-Authorization", auth(sub))
                contentType = MediaType.APPLICATION_JSON
                content = bridge
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("RAINBOW_BRIDGE") }
                jsonPath("$.leftAt") { value("2026-03-01") }
                jsonPath("$.farewellMessage") { value("Sleep tight") }
            }
        mockMvc
            .post("/v1/stuffies/$id/rainbow-bridge") {
                header("X-Authorization", auth(sub))
                contentType = MediaType.APPLICATION_JSON
                content = bridge
            }.andExpect { status { isConflict() } }
        mockMvc
            .get("/v1/stuffies?status=ACTIVE") { header("X-Authorization", auth(sub)) }
            .andExpect { content { json("[]") } }

        // sharing is idempotent and the public page shows the note without owner data
        val share = { enabled: Boolean ->
            mockMvc.put("/v1/stuffies/$id/sharing") {
                header("X-Authorization", auth(sub))
                contentType = MediaType.APPLICATION_JSON
                content = """{"enabled": $enabled}"""
            }
        }
        val token = "\"shareToken\":\"([^\"]+)\"".toRegex().find(share(true).andReturn().response.contentAsString)!!.groupValues[1]
        assertTrue(share(true).andReturn().response.contentAsString.contains(token))

        val shared = mockMvc.get("/v1/shared/$token").andExpect { // no token header: public
            status { isOk() }
            jsonPath("$.name") { value("Mr. Bunny") }
            jsonPath("$.status") { value("RAINBOW_BRIDGE") }
            jsonPath("$.farewellMessage") { value("Sleep tight") }
            jsonPath("$.shareToken") { doesNotExist() }
            jsonPath("$.ownerSub") { doesNotExist() }
        }.andReturn()
        assertFalse(shared.response.contentAsString.contains(sub))

        // restore clears the note but keeps the history
        mockMvc.post("/v1/stuffies/$id/restore") { header("X-Authorization", auth(sub)) }.andExpect {
            jsonPath("$.status") { value("ACTIVE") }
            jsonPath("$.leftAt") { value(null) }
            jsonPath("$.visits.length()") { value(1) }
            jsonPath("$.visits[0].returnedAt") { exists() }
        }

        share(false).andExpect { status { isOk() } }
        mockMvc.get("/v1/shared/$token").andExpect { status { isNotFound() } }
    }

    @Test
    fun `users cannot see or change each others stuffies`() {
        val owner = newUser()
        val other = newUser()
        val id = create(owner, """{"name": "Mine"}""")

        mockMvc.get("/v1/stuffies") { header("X-Authorization", auth(other)) }.andExpect { content { json("[]") } }
        mockMvc.get("/v1/stuffies/$id") { header("X-Authorization", auth(other)) }.andExpect { status { isNotFound() } }
        mockMvc
            .put("/v1/stuffies/$id") {
                header("X-Authorization", auth(other))
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "Hacked"}"""
            }.andExpect { status { isNotFound() } }
        mockMvc
            .put("/v1/stuffies/$id/sharing") {
                header("X-Authorization", auth(other))
                contentType = MediaType.APPLICATION_JSON
                content = """{"enabled": true}"""
            }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `photos are resized, private to the owner and reachable through a share link`() {
        val owner = newUser()
        val other = newUser()

        val uploaded = upload(owner, png(2000, 1000))
        assertEquals(201, uploaded.response.status)
        val photoId = uploadedId(uploaded)

        val bytes = mockMvc.get("/v1/photos/$photoId") { header("X-Authorization", auth(owner)) }.andExpect {
            status { isOk() }
            content { contentType("image/jpeg") }
        }.andReturn().response.contentAsByteArray
        val image = ImageIO.read(ByteArrayInputStream(bytes))
        assertEquals(1200, image.width)

        mockMvc.get("/v1/photos/$photoId") { header("X-Authorization", auth(other)) }.andExpect { status { isNotFound() } }
        mockMvc
            .post("/v1/stuffies") {
                header("X-Authorization", auth(other))
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "Thief", "photoId": "$photoId"}"""
            }.andExpect { status { isBadRequest() } }

        val id = create(owner, """{"name": "Pic", "photoId": "$photoId"}""")
        val token = "\"shareToken\":\"([^\"]+)\"".toRegex().find(
            mockMvc
                .put("/v1/stuffies/$id/sharing") {
                    header("X-Authorization", auth(owner))
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"enabled": true}"""
                }.andReturn().response.contentAsString,
        )!!.groupValues[1]
        mockMvc.get("/v1/shared/$token/photo").andExpect {
            status { isOk() }
            content { contentType("image/jpeg") }
        }

        // removing the photo deletes it
        mockMvc
            .put("/v1/stuffies/$id") {
                header("X-Authorization", auth(owner))
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "Pic", "removePhoto": true}"""
            }.andExpect { jsonPath("$.photoId") { value(null) } }
        mockMvc.get("/v1/photos/$photoId") { header("X-Authorization", auth(owner)) }.andExpect { status { isNotFound() } }
        mockMvc.get("/v1/shared/$token/photo").andExpect { status { isNotFound() } }
    }

    @Test
    fun `rejects uploads that are not images`() {
        val result = upload(newUser(), "definitely not an image".toByteArray(), "x.txt")
        assertEquals(400, result.response.status)
    }

    @Test
    fun `account erasure removes everything, leaves a hashed tombstone and can be replayed`() {
        val gone = newUser()
        val kept = newUser()
        val photoId = uploadedId(upload(gone, png(100, 100)))
        val goneId = create(gone, """{"name": "Bye", "photoId": "$photoId"}""")
        mockMvc
            .put("/v1/stuffies/$goneId/sharing") {
                header("X-Authorization", auth(gone))
                contentType = MediaType.APPLICATION_JSON
                content = """{"enabled": true}"""
            }
        val keptPhoto = uploadedId(upload(kept, png(100, 100)))
        create(kept, """{"name": "Stays", "photoId": "$keptPhoto"}""")

        mockMvc.delete("/v1/account") { header("X-Authorization", auth(gone)) }.andExpect { status { isNoContent() } }

        mockMvc.get("/v1/stuffies") { header("X-Authorization", auth(gone)) }.andExpect { content { json("[]") } }
        mockMvc.get("/v1/photos/$photoId") { header("X-Authorization", auth(gone)) }.andExpect { status { isNotFound() } }
        mockMvc.get("/v1/stuffies") { header("X-Authorization", auth(kept)) }.andExpect { jsonPath("$.length()") { value(1) } }
        mockMvc.get("/v1/photos/$keptPhoto") { header("X-Authorization", auth(kept)) }.andExpect { status { isOk() } }

        val hash = subHasher.hash(gone)
        assertTrue(tombstones.existsBySubHash(hash))
        assertFalse(tombstones.findAll().any { it.subHash.contains(gone) })

        // a restored backup brings the data back; the replay erases it again
        val now = Instant.now()
        stuffieRepository.save(
            StuffieModel(null, gone, "Zombie", null, null, null, null, null, null, null, null, null, StuffieStatus.ACTIVE, null, null, now, now),
        )
        assertEquals(1, stuffieRepository.findByOwnerSub(gone).size)
        assertTrue(erasureService.replay() >= 1)
        assertEquals(0, stuffieRepository.findByOwnerSub(gone).size)
        assertEquals(1, stuffieRepository.findByOwnerSub(kept).size)
        assertNotNull(tombstones.findAll().firstOrNull { it.subHash == hash })
    }
}
