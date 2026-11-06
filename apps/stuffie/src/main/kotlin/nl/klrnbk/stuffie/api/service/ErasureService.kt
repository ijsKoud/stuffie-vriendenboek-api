package nl.klrnbk.stuffie.api.service

import nl.klrnbk.stuffie.datasource.models.ErasureTombstoneModel
import nl.klrnbk.stuffie.datasource.repositories.ErasureTombstoneRepository
import nl.klrnbk.stuffie.datasource.repositories.PhotoRepository
import nl.klrnbk.stuffie.datasource.repositories.RainbowVisitRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieShareRepository
import nl.klrnbk.stuffie.helpers.SubHasher
import nl.klrnbk.stuffie.storage.PhotoStorage
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant

@Service
class ErasureService(
    private val stuffieRepository: StuffieRepository,
    private val shareRepository: StuffieShareRepository,
    private val visitRepository: RainbowVisitRepository,
    private val photoRepository: PhotoRepository,
    private val tombstoneRepository: ErasureTombstoneRepository,
    private val photoStorage: PhotoStorage,
    private val subHasher: SubHasher,
    private val transactionTemplate: TransactionTemplate,
) {
    /**
     * Permanently removes everything of one OIDC subject (stuffies, visits, share links, photos) and writes a
     * hashed tombstone, so [replay] can redo it after a backup restore. Safe to call again after a partial failure.
     */
    fun erase(ownerSub: String) {
        val hash = subHasher.hash(ownerSub)
        photoStorage.deletePrefix("$hash/")

        transactionTemplate.executeWithoutResult {
            val ids = stuffieRepository.findByOwnerSub(ownerSub).map { it.id!! }
            if (ids.isNotEmpty()) {
                visitRepository.deleteByStuffieIdIn(ids)
                shareRepository.deleteByStuffieIdIn(ids)
            }
            stuffieRepository.deleteByOwnerSub(ownerSub)
            photoRepository.deleteByOwnerSub(ownerSub)

            if (!tombstoneRepository.existsBySubHash(hash)) {
                tombstoneRepository.save(ErasureTombstoneModel(null, hash, Instant.now()))
            }
        }
    }

    /** Re-applies every recorded erasure. Returns the number of subjects that came back and were erased again. */
    fun replay(): Int {
        val hashes = tombstoneRepository.findAll().map { it.subHash }.toSet()
        if (hashes.isEmpty()) return 0

        val subs = (stuffieRepository.findDistinctOwnerSubs() + photoRepository.findDistinctOwnerSubs()).toSet()
        val resurrected = subs.filter { subHasher.hash(it) in hashes }
        resurrected.forEach(::erase)

        return resurrected.size
    }
}
