package nl.klrnbk.stuffie.api.facade

import nl.klrnbk.stuffie.api.service.ErasureService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AccountFacade(
    private val erasureService: ErasureService,
) {
    private val logger = LoggerFactory.getLogger(AccountFacade::class.java)

    fun eraseAccount(userId: String): ResponseEntity<Unit> {
        erasureService.erase(userId)
        logger.info("Erased all data of a user (GDPR request)")

        return ResponseEntity.noContent().build()
    }
}
