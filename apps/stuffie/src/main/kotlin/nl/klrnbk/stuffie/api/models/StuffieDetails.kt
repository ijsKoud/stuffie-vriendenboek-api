package nl.klrnbk.stuffie.api.models

import nl.klrnbk.stuffie.datasource.models.RainbowVisitModel
import nl.klrnbk.stuffie.datasource.models.StuffieModel
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import java.time.Instant
import java.time.LocalDate

data class RainbowVisitDetails(
    val id: String,
    val leftAt: LocalDate,
    val farewellMessage: String?,
    val returnedAt: LocalDate?,
) {
    companion object {
        fun fromModel(model: RainbowVisitModel) =
            RainbowVisitDetails(
                id = model.id!!,
                leftAt = model.leftAt,
                farewellMessage = model.farewellMessage,
                returnedAt = model.returnedAt,
            )
    }
}

/** What the owner sees. */
data class StuffieDetails(
    val id: String,
    val name: String,
    /** Download with `GET /v1/photos/{photoId}`. */
    val photoId: String?,
    val species: String?,
    val birthday: LocalDate?,
    val givenBy: String?,
    val favouriteFood: String?,
    val hobbies: String?,
    val laterWordIk: String?,
    val howWeMet: String?,
    val whyILoveThem: String?,
    val status: StuffieStatus,
    val leftAt: LocalDate?,
    val farewellMessage: String?,
    val visits: List<RainbowVisitDetails>,
    /** Present while sharing is enabled. */
    val shareToken: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun fromModel(
            model: StuffieModel,
            visits: List<RainbowVisitModel>,
            shareToken: String?,
        ) = StuffieDetails(
            id = model.id!!,
            name = model.name,
            photoId = model.photoId,
            species = model.species,
            birthday = model.birthday,
            givenBy = model.givenBy,
            favouriteFood = model.favouriteFood,
            hobbies = model.hobbies,
            laterWordIk = model.laterWordIk,
            howWeMet = model.howWeMet,
            whyILoveThem = model.whyILoveThem,
            status = model.status,
            leftAt = model.leftAt,
            farewellMessage = model.farewellMessage,
            visits = visits.sortedByDescending { it.createdAt }.map(RainbowVisitDetails::fromModel),
            shareToken = shareToken,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
        )
    }
}

/** What anyone holding the share link sees: no owner data and no share token. */
data class SharedStuffieDetails(
    val id: String,
    val name: String,
    /** Download with `GET /v1/shared/{token}/photo`. */
    val hasPhoto: Boolean,
    val species: String?,
    val birthday: LocalDate?,
    val givenBy: String?,
    val favouriteFood: String?,
    val hobbies: String?,
    val laterWordIk: String?,
    val howWeMet: String?,
    val whyILoveThem: String?,
    val status: StuffieStatus,
    val leftAt: LocalDate?,
    val farewellMessage: String?,
    val visits: List<RainbowVisitDetails>,
) {
    companion object {
        fun fromDetails(details: StuffieDetails) =
            SharedStuffieDetails(
                id = details.id,
                name = details.name,
                hasPhoto = details.photoId != null,
                species = details.species,
                birthday = details.birthday,
                givenBy = details.givenBy,
                favouriteFood = details.favouriteFood,
                hobbies = details.hobbies,
                laterWordIk = details.laterWordIk,
                howWeMet = details.howWeMet,
                whyILoveThem = details.whyILoveThem,
                status = details.status,
                leftAt = details.leftAt,
                farewellMessage = details.farewellMessage,
                visits = details.visits,
            )
    }
}

data class PhotoUploadResponse(
    val id: String,
)
