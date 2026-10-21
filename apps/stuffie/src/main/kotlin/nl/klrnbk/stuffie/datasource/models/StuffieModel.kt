package nl.klrnbk.stuffie.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "stuffies", indexes = [Index(name = "stuffies_owner_idx", columnList = "owner_sub")])
data class StuffieModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,

    /** The OIDC `sub` of the owner. */
    @Column(name = "owner_sub", nullable = false)
    val ownerSub: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "photo_id")
    val photoId: String?,

    @Column(name = "species")
    val species: String?,

    /** The day they came to the owner. */
    @Column(name = "birthday")
    val birthday: LocalDate?,

    @Column(name = "given_by")
    val givenBy: String?,

    @Column(name = "favourite_food")
    val favouriteFood: String?,

    @Column(name = "hobbies")
    val hobbies: String?,

    @Column(name = "later_word_ik")
    val laterWordIk: String?,

    @Column(name = "how_we_met", length = 2000)
    val howWeMet: String?,

    @Column(name = "why_i_love_them", length = 2000)
    val whyILoveThem: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: StuffieStatus,

    @Column(name = "left_at")
    val leftAt: LocalDate?,

    @Column(name = "farewell_message", length = 280)
    val farewellMessage: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
