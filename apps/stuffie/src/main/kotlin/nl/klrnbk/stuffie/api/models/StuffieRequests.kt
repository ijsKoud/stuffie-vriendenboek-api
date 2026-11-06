package nl.klrnbk.stuffie.api.models

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

/** Used for both create and update (an update replaces all fields). Blank text clears a field. */
data class StuffieRequest(
    @field:NotBlank
    @field:Size(max = 80)
    val name: String,

    /** A photo id returned by `POST /v1/photos`; attaches (and replaces) the photo. Omit to keep the current one. */
    val photoId: String? = null,

    /** Removes the current photo. Cannot be combined with [photoId]. */
    val removePhoto: Boolean = false,

    @field:Size(max = 80)
    val species: String? = null,

    /** The day they came to you. */
    val birthday: LocalDate? = null,

    @field:Size(max = 80)
    val givenBy: String? = null,

    @field:Size(max = 120)
    val favouriteFood: String? = null,

    @field:Size(max = 200)
    val hobbies: String? = null,

    @field:Size(max = 120)
    val laterWordIk: String? = null,

    @field:Size(max = 2000)
    val howWeMet: String? = null,

    @field:Size(max = 2000)
    val whyILoveThem: String? = null,
)

data class RainbowBridgeRequest(
    @field:NotNull
    val leftAt: LocalDate?,

    @field:Size(max = 280)
    val farewellMessage: String? = null,
)

data class SharingRequest(
    val enabled: Boolean,
)
