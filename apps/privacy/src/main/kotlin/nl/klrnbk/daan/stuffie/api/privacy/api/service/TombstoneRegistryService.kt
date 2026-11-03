package nl.klrnbk.daan.stuffie.api.privacy.api.service

import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.ErasureTombstoneModel
import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.repositories.ErasureTombstoneRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TombstoneRegistryService(
    private val erasureTombstoneRepository: ErasureTombstoneRepository,
) {
    fun findAll(): List<ErasureTombstoneModel> = erasureTombstoneRepository.findAll()

    /** A user that came back and asks again gets a newer `deletedAt`, which makes every service erase again. */
    fun createOrUpdateDeleteRequest(
        hashedUserSub: String,
        // millis: what the database keeps, so a stored copy compares equal
        deletedAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    ): ErasureTombstoneModel {
        val existing = erasureTombstoneRepository.findFirstBySubHash(hashedUserSub)
        val entity = existing?.copy(deletedAt = deletedAt) ?: ErasureTombstoneModel(null, hashedUserSub, deletedAt)

        return erasureTombstoneRepository.save(entity)
    }
}
