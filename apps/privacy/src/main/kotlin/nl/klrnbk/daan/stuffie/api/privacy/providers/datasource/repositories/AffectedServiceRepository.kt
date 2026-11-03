package nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.repositories

import nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models.AffectedServiceModel
import org.springframework.data.jpa.repository.JpaRepository

interface AffectedServiceRepository : JpaRepository<AffectedServiceModel, String> {
    fun findByServiceName(serviceName: String): List<AffectedServiceModel>
}
