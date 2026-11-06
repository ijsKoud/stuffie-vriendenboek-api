package nl.klrnbk.daan.stuffie.api.privacy.api.facade

import nl.klrnbk.common.exceptions.ApiException
import nl.klrnbk.daan.stuffie.api.privacy.api.service.ErasureReplayRunnerService
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class ScheduledTasksFacade(
    private val erasureReplayRunnerService: ErasureReplayRunnerService,
    private val properties: ApplicationProperties,
) {
    /** Full replay (like a startup): every platform gets every tombstone again. */
    fun runErasureReplay(apiKey: String?): ResponseEntity<Unit> {
        requireValidApiKey(apiKey)
        erasureReplayRunnerService.replay(force = true)

        return ResponseEntity.noContent().build()
    }

    private fun requireValidApiKey(apiKey: String?) {
        // constant-time compare, so the key cannot be guessed byte by byte from response times
        val valid =
            apiKey != null &&
                MessageDigest.isEqual(apiKey.toByteArray(), properties.privacy.scheduledApiKey.toByteArray())
        if (!valid) throw ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized.", "Missing or invalid x-api-key header.")
    }
}
