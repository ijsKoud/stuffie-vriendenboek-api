package nl.klrnbk.stuffie.api.service

import nl.klrnbk.common.exceptions.BadRequestException
import nl.klrnbk.common.exceptions.ConflictException
import nl.klrnbk.common.exceptions.NotFoundException
import nl.klrnbk.stuffie.api.models.RainbowBridgeRequest
import nl.klrnbk.stuffie.api.models.SharedStuffieDetails
import nl.klrnbk.stuffie.api.models.StuffieDetails
import nl.klrnbk.stuffie.api.models.StuffieRequest
import nl.klrnbk.stuffie.datasource.models.RainbowVisitModel
import nl.klrnbk.stuffie.datasource.models.StuffieModel
import nl.klrnbk.stuffie.datasource.models.StuffieShareModel
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import nl.klrnbk.stuffie.datasource.repositories.RainbowVisitRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieShareRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.util.Base64

@Service
class StuffieService(
    private val stuffieRepository: StuffieRepository,
    private val shareRepository: StuffieShareRepository,
    private val visitRepository: RainbowVisitRepository,
    private val photoService: PhotoService,
) {
    fun list(
        ownerSub: String,
        status: StuffieStatus?,
    ): List<StuffieDetails> {
        val models =
            if (status == null) {
                stuffieRepository.findByOwnerSubOrderByCreatedAtDesc(ownerSub)
            } else {
                stuffieRepository.findByOwnerSubAndStatusOrderByCreatedAtDesc(ownerSub, status)
            }

        return toDetails(models)
    }

    fun get(
        ownerSub: String,
        id: String,
    ): StuffieDetails = toDetails(listOf(ownedStuffie(ownerSub, id))).first()

    @Transactional
    fun create(
        ownerSub: String,
        request: StuffieRequest,
    ): StuffieDetails {
        if (request.removePhoto) throw BadRequestException("removePhoto makes no sense for a new stuffie.")
        request.photoId?.let { photoService.assertOwned(ownerSub, it) }

        val now = Instant.now()
        val saved =
            stuffieRepository.save(
                StuffieModel(
                    id = null,
                    ownerSub = ownerSub,
                    name = request.name.trim(),
                    photoId = request.photoId,
                    species = request.species.blankToNull(),
                    birthday = request.birthday,
                    givenBy = request.givenBy.blankToNull(),
                    favouriteFood = request.favouriteFood.blankToNull(),
                    hobbies = request.hobbies.blankToNull(),
                    laterWordIk = request.laterWordIk.blankToNull(),
                    howWeMet = request.howWeMet.blankToNull(),
                    whyILoveThem = request.whyILoveThem.blankToNull(),
                    status = StuffieStatus.ACTIVE,
                    leftAt = null,
                    farewellMessage = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

        return toDetails(listOf(saved)).first()
    }

    @Transactional
    fun update(
        ownerSub: String,
        id: String,
        request: StuffieRequest,
    ): StuffieDetails {
        if (request.removePhoto && request.photoId != null) throw BadRequestException("Use either photoId or removePhoto, not both.")

        val existing = ownedStuffie(ownerSub, id)
        // Read before saving: merging the copy writes its state into the managed `existing` instance.
        val previousPhotoId = existing.photoId
        request.photoId?.let { photoService.assertOwned(ownerSub, it) }

        val photoId =
            when {
                request.removePhoto -> null
                request.photoId != null -> request.photoId
                else -> existing.photoId
            }

        val saved =
            stuffieRepository.save(
                existing.copy(
                    name = request.name.trim(),
                    photoId = photoId,
                    species = request.species.blankToNull(),
                    birthday = request.birthday,
                    givenBy = request.givenBy.blankToNull(),
                    favouriteFood = request.favouriteFood.blankToNull(),
                    hobbies = request.hobbies.blankToNull(),
                    laterWordIk = request.laterWordIk.blankToNull(),
                    howWeMet = request.howWeMet.blankToNull(),
                    whyILoveThem = request.whyILoveThem.blankToNull(),
                    updatedAt = Instant.now(),
                ),
            )
        previousPhotoId?.takeIf { it != photoId }?.let(photoService::delete)

        return toDetails(listOf(saved)).first()
    }

    @Transactional
    fun sendToRainbowBridge(
        ownerSub: String,
        id: String,
        request: RainbowBridgeRequest,
    ): StuffieDetails {
        val existing = ownedStuffie(ownerSub, id)
        if (existing.status == StuffieStatus.RAINBOW_BRIDGE) throw ConflictException("Already on the Rainbow Bridge.")

        val leftAt = request.leftAt!!
        val message = request.farewellMessage.blankToNull()
        val now = Instant.now()

        val saved =
            stuffieRepository.save(
                existing.copy(status = StuffieStatus.RAINBOW_BRIDGE, leftAt = leftAt, farewellMessage = message, updatedAt = now),
            )
        visitRepository.save(RainbowVisitModel(null, existing.id!!, leftAt, message, null, now))

        return toDetails(listOf(saved)).first()
    }

    /** Brings them home: the note is cleared, the visit stays in the history. */
    @Transactional
    fun restore(
        ownerSub: String,
        id: String,
    ): StuffieDetails {
        val existing = ownedStuffie(ownerSub, id)
        if (existing.status != StuffieStatus.RAINBOW_BRIDGE) throw ConflictException("Not on the Rainbow Bridge.")

        val saved =
            stuffieRepository.save(
                existing.copy(status = StuffieStatus.ACTIVE, leftAt = null, farewellMessage = null, updatedAt = Instant.now()),
            )
        visitRepository
            .findByStuffieIdAndReturnedAtIsNull(existing.id!!)
            .forEach { visitRepository.save(it.copy(returnedAt = LocalDate.now())) }

        return toDetails(listOf(saved)).first()
    }

    /** Idempotent: enabling twice keeps the same token, disabling revokes the link. */
    @Transactional
    fun setSharing(
        ownerSub: String,
        id: String,
        enabled: Boolean,
    ): StuffieDetails {
        val existing = ownedStuffie(ownerSub, id)
        val share = shareRepository.findByStuffieId(existing.id!!).firstOrNull()

        if (enabled && share == null) {
            shareRepository.save(StuffieShareModel(null, existing.id!!, newShareToken()))
        } else if (!enabled && share != null) {
            shareRepository.delete(share)
        }

        return toDetails(listOf(existing)).first()
    }

    fun getShared(token: String): SharedStuffieDetails {
        val share = shareRepository.findByToken(token).firstOrNull() ?: throw NotFoundException("This page is not shared.")
        val stuffie = stuffieRepository.findById(share.stuffieId).orElse(null) ?: throw NotFoundException("This page is not shared.")

        return SharedStuffieDetails.fromDetails(toDetails(listOf(stuffie)).first())
    }

    private fun ownedStuffie(
        ownerSub: String,
        id: String,
    ): StuffieModel = stuffieRepository.findByIdAndOwnerSub(id, ownerSub).firstOrNull() ?: throw NotFoundException("Stuffie not found.")

    /** Loads visits and share tokens for all models in two queries. */
    private fun toDetails(models: List<StuffieModel>): List<StuffieDetails> {
        if (models.isEmpty()) return listOf()

        val ids = models.map { it.id!! }
        val visits = visitRepository.findByStuffieIdIn(ids).groupBy { it.stuffieId }
        val shares = shareRepository.findByStuffieIdIn(ids).associateBy { it.stuffieId }

        return models.map { StuffieDetails.fromModel(it, visits[it.id].orEmpty(), shares[it.id]?.token) }
    }

    private fun newShareToken(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
