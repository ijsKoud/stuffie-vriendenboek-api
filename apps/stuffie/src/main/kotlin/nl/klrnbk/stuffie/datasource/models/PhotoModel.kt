package nl.klrnbk.stuffie.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "photos")
data class PhotoModel(
    /** Assigned by the service: the id is part of the storage key, which is written before the row. */
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "owner_sub", nullable = false)
    val ownerSub: String,

    /** Object key in the photo storage: `<owner hash>/<id>.jpg`. */
    @Column(name = "storage_key", nullable = false)
    val storageKey: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
