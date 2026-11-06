package nl.klrnbk.stuffie.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

/** One stay on the Rainbow Bridge. `returnedAt` is null while the stuffie is still there. */
@Entity
@Table(name = "stuffie_rainbow_visits")
data class RainbowVisitModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,

    @Column(name = "stuffie_id", nullable = false)
    val stuffieId: String,

    @Column(name = "left_at", nullable = false)
    val leftAt: LocalDate,

    @Column(name = "farewell_message", length = 280)
    val farewellMessage: String?,

    @Column(name = "returned_at")
    val returnedAt: LocalDate?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
