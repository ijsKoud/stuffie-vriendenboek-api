package nl.klrnbk.stuffie.datasource.repositories

import nl.klrnbk.stuffie.datasource.models.ErasureTombstoneModel
import org.springframework.data.jpa.repository.JpaRepository

interface ErasureTombstoneRepository : JpaRepository<ErasureTombstoneModel, String> {
    fun existsBySubHash(subHash: String): Boolean
}
