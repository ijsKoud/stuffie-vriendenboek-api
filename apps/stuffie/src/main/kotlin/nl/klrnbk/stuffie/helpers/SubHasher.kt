package nl.klrnbk.stuffie.helpers

import nl.klrnbk.stuffie.config.StuffieProperties
import org.springframework.stereotype.Component
import java.security.MessageDigest

/**
 * Pseudonymous, salted hash of an OIDC `sub`. Used for photo folder names and erasure tombstones so neither
 * contains the raw identifier. Same construction as the previous web implementation (sha256 of "<salt>:<sub>").
 */
@Component
class SubHasher(
    private val properties: StuffieProperties,
) {
    fun hash(sub: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest("${properties.privacy.subHashSalt}:$sub".toByteArray())
            .joinToString("") { "%02x".format(it) }
}
