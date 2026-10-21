package nl.klrnbk.stuffie.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "photos")
data class PhotoModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,

    @Column(name = "owner_sub", nullable = false)
    val ownerSub: String,

    /** Object key in the photo storage: `<owner hash>/<id>.jpg`. */
    @Column(name = "storage_key", nullable = false)
    val storageKey: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
