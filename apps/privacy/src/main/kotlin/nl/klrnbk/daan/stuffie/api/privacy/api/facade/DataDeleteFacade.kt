package nl.klrnbk.daan.stuffie.api.privacy.api.facade

import nl.klrnbk.common.helpers.hash
import nl.klrnbk.daan.stuffie.api.privacy.api.service.PlatformDataDeletionService
import nl.klrnbk.daan.stuffie.api.privacy.api.service.TombstoneRegistryService
import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DataDeleteFacade(
    private val tombstoneRegistryService: TombstoneRegistryService,
    private val platformDataDeletionService: PlatformDataDeletionService,
    private val properties: ApplicationProperties,
) {
    private val logger = LoggerFactory.getLogger(DataDeleteFacade::class.java)

    /** 204 when every platform confirmed, 202 when the request is recorded and some platform is still to be retried. */
    fun requestDataDeletion(authentication: JwtAuthenticationToken): ResponseEntity<Unit> {
        val hashedUserSub = hash(authentication.principal, properties.privacy.subHashSalt)
        val tombstone = tombstoneRegistryService.createOrUpdateDeleteRequest(hashedUserSub)
        val complete = platformDataDeletionService.erase(listOf(tombstone))
        logger.info("Data deletion requested (GDPR), complete=$complete")

        return if (complete) ResponseEntity.noContent().build() else ResponseEntity.accepted().build()
    }
}
