package nl.klrnbk.daan.stuffie.api.privacy

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import nl.klrnbk.daan.stuffie.api.privacy.mocks.MockPlatformClient
import nl.klrnbk.security.idp.client.openid.OpenIdClient
import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponse
import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponseKeys
import nl.klrnbk.security.idp.config.SecurityConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID
import kotlin.test.BeforeTest

@SpringBootTest
@ActiveProfiles("test")
@Import(FakeConfiguration::class)
abstract class BaseTest {
    @Autowired lateinit var platformClient: MockPlatformClient

    @Autowired lateinit var fakeConfiguration: FakeConfiguration

    fun token(sub: String) = fakeConfiguration.token(sub)

    @BeforeTest
    fun cleanSlate() {
        platformClient.forgetRequests()
        platformClient.bringUp()
    }

    fun newSub() = "user-${UUID.randomUUID()}"

    fun newHash() = "hash-${UUID.randomUUID()}"
}

@TestConfiguration
class FakeConfiguration {
    @Bean
    @Primary
    fun fakePlatformClient(properties: ApplicationProperties) = MockPlatformClient(properties)

    private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun unsigned(value: java.math.BigInteger): String {
        val bytes = value.toByteArray().let { if (it[0] == 0.toByte()) it.copyOfRange(1, it.size) else it }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun token(sub: String): String =
        JWT
            .create()
            .withKeyId("test-key")
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withSubject(sub)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
            .sign(Algorithm.RSA256(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey))

    /** Serves the test key instead of fetching a JWKS from an IdP. */
    @Bean
    @Primary
    fun testOpenIdClient(properties: SecurityConfigurationProperties): OpenIdClient {
        val public = keyPair.public as RSAPublicKey
        val jwks =
            OpenIdJwksResponse(
                listOf(
                    OpenIdJwksResponseKeys("RS256", "test-key", "RSA", "sig", unsigned(public.modulus), unsigned(public.publicExponent)),
                ),
            )

        return object : OpenIdClient(properties) {
            override fun getFreshJwks() = jwks
        }
    }
}
