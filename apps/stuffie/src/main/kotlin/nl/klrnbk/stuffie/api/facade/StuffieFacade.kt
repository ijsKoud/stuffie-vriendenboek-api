package nl.klrnbk.stuffie.api.facade

import nl.klrnbk.stuffie.api.models.RainbowBridgeRequest
import nl.klrnbk.stuffie.api.models.StuffieDetails
import nl.klrnbk.stuffie.api.models.StuffieRequest
import nl.klrnbk.stuffie.api.service.StuffieService
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class StuffieFacade(
    private val stuffieService: StuffieService,
) {
    private val logger = LoggerFactory.getLogger(StuffieFacade::class.java)

    fun list(
        userId: String,
        status: StuffieStatus?,
    ): List<StuffieDetails> = stuffieService.list(userId, status)

    fun get(
        userId: String,
        id: String,
    ): StuffieDetails = stuffieService.get(userId, id)

    fun create(
        userId: String,
        request: StuffieRequest,
    ): ResponseEntity<StuffieDetails> {
        val created = stuffieService.create(userId, request)
        logger.info("Created stuffie=${created.id} for user=$userId")

        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    fun update(
        userId: String,
        id: String,
        request: StuffieRequest,
    ): StuffieDetails = stuffieService.update(userId, id, request)

    fun sendToRainbowBridge(
        userId: String,
        id: String,
        request: RainbowBridgeRequest,
    ): StuffieDetails {
        val result = stuffieService.sendToRainbowBridge(userId, id, request)
        logger.info("Stuffie=$id of user=$userId crossed the Rainbow Bridge")

        return result
    }

    fun restore(
        userId: String,
        id: String,
    ): StuffieDetails {
        val result = stuffieService.restore(userId, id)
        logger.info("Stuffie=$id of user=$userId came back from the Rainbow Bridge")

        return result
    }

    fun setSharing(
        userId: String,
        id: String,
        enabled: Boolean,
    ): StuffieDetails {
        val result = stuffieService.setSharing(userId, id, enabled)
        logger.info("Sharing of stuffie=$id set to enabled=$enabled by user=$userId")

        return result
    }
}
