package nl.klrnbk.stuffie.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "stuffie_shares")
data class StuffieShareModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,

    @Column(name = "stuffie_id", nullable = false, unique = true)
    val stuffieId: String,

    @Column(name = "token", nullable = false, unique = true)
    val token: String,
)
