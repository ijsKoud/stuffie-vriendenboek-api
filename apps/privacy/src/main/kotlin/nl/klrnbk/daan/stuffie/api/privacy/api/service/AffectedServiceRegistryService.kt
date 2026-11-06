package nl.klrnbk.daan.stuffie.api.privacy.api.service

import nl.klrnbk.common.Platforms
import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.AffectedServiceModel
import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.ErasureTombstoneModel
import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.repositories.AffectedServiceRepository
import org.springframework.stereotype.Service

@Service
class AffectedServiceRegistryService(
    private val affectedServiceRepository: AffectedServiceRepository,
) {
    /** Tombstones the platform has not confirmed yet, or only confirmed for an older deletion request. */
    fun pending(
        tombstones: List<ErasureTombstoneModel>,
        platform: Platforms,
    ): List<ErasureTombstoneModel> {
        val confirmed = affectedServiceRepository.findByServiceName(platform.name).associate { it.tombstoneId to it.deletedAt }
        return tombstones.filter { confirmed[it.id]?.isBefore(it.deletedAt) ?: true }
    }

    fun markDeleted(
        platform: Platforms,
        tombstones: List<ErasureTombstoneModel>,
    ) {
        val existing = affectedServiceRepository.findByServiceName(platform.name).associateBy { it.tombstoneId }
        affectedServiceRepository.saveAll(
            tombstones.map {
                existing[it.id]?.copy(deletedAt = it.deletedAt) ?: AffectedServiceModel(null, it.id!!, platform.name, it.deletedAt)
            },
        )
    }
}
