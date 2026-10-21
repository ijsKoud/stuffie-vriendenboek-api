package nl.klrnbk.security.idp.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import nl.klrnbk.security.idp.client.openid.OpenIdClient
import nl.klrnbk.security.idp.client.openid.models.OpenIdJwksResponseKeys
import nl.klrnbk.security.idp.exceptions.JwtVerifyException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Service
class OpenIdService(
    val client: OpenIdClient,
) {
    var jwks: List<OpenIdJwksResponseKeys> = getFreshJwks()
    private var lastRefresh: Instant = Instant.now()

    @Scheduled(cron = "\${stuffie.spring.security.authorization.jwks.refresh-interval}")
    private fun updateJwks() {
        jwks = getFreshJwks()
        lastRefresh = Instant.now()
    }

    fun verifyJwt(token: String): Boolean {
        try {
            val result = decodeAndVerifyJwt(token)
            return result != null
        } catch (_: Exception) {
            return false
        }
    }

    fun decodeAndVerifyJwt(token: String): DecodedJWT? {
        val jwt = JWT.decode(token)
        val kid = jwt.keyId ?: throw JwtVerifyException("No 'kid' in token header")

        val jwk = findJwk(kid) ?: throw JwtVerifyException("No matching JWK found for kid=$kid")
        val publicKey = getPublicKey(jwk)

        val authorization = client.properties.authorization
        val algorithm = Algorithm.RSA256(publicKey, null)
        val verifier =
            JWT
                .require(algorithm)
                .withIssuer(authorization.issuer)
                .withAudience(authorization.audience)
                .acceptIssuedAt(1L)
                .build()

        return verifier.verify(token)
    }

    fun getFreshJwks(): List<OpenIdJwksResponseKeys> = client.getFreshJwks().keys

    /** An unknown kid usually means the IdP rotated its keys: refresh once, but at most once a minute. */
    private fun findJwk(kid: String): OpenIdJwksResponseKeys? {
        jwks.find { it.kid == kid }?.let { return it }
        if (Duration.between(lastRefresh, Instant.now()) < Duration.ofMinutes(1)) return null

        updateJwks()
        return jwks.find { it.kid == kid }
    }

    private fun getPublicKey(jwk: OpenIdJwksResponseKeys): RSAPublicKey {
        val nBytes = Base64.getUrlDecoder().decode(jwk.n)
        val eBytes = Base64.getUrlDecoder().decode(jwk.e)
        val spec = RSAPublicKeySpec(BigInteger(1, nBytes), BigInteger(1, eBytes))

        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }
}
