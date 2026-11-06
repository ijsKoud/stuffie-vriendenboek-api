package nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.repositories

import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.ErasureTombstoneModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface ErasureTombstoneRepository : JpaRepository<ErasureTombstoneModel, String> {
    @Transactional
    fun findFirstBySubHash(subHash: String): ErasureTombstoneModel?
}
