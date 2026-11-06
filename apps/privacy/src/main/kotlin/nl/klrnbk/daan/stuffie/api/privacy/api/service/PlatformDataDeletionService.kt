package nl.klrnbk.daan.stuffie.api.privacy.api.service

import nl.klrnbk.daan.stuffie.api.privacy.providers.clients.internalservices.PlatformClient
import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.ErasureTombstoneModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PlatformDataDeletionService(
    private val platformClient: PlatformClient,
    private val affectedServiceRegistryService: AffectedServiceRegistryService,
) {
    private val logger = LoggerFactory.getLogger(PlatformDataDeletionService::class.java)

    /**
     * Asks every platform to erase these tombstones and remembers who confirmed. With [force] the platforms that
     * already confirmed are asked again, because a restored backup of theirs may have brought the data back.
     * Returns true when every platform confirmed; the rest stays pending for the next retry.
     */
    fun erase(
        tombstones: List<ErasureTombstoneModel>,
        force: Boolean = false,
    ): Boolean =
        platformClient.platforms
            .map { platform ->
                val todo = if (force) tombstones else affectedServiceRegistryService.pending(tombstones, platform)
                todo.isEmpty() ||
                    runCatching {
                        // shortcut: one request per 500 hashes, upgrade when a platform cannot keep up
                        todo.chunked(500).forEach { platformClient.erase(platform, it.map(ErasureTombstoneModel::subHash)) }
                        affectedServiceRegistryService.markDeleted(platform, todo)
                    }.onFailure { logger.error("Erasure at $platform failed for ${todo.size} account(s), will retry", it) }
                        .isSuccess
            }.all { it }
}
