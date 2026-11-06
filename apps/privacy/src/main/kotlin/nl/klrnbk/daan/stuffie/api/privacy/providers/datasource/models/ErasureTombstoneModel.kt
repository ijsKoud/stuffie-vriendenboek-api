package nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/** GDPR: the only trace of an erased account. Replayed at startup so a restored backup cannot resurrect them. */
@Entity
@Table(name = "privacy_erasure_tombstones")
data class ErasureTombstoneModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,
    @Column(name = "sub_hash", nullable = false, unique = true)
    val subHash: String,
    // This date should be synced with the affected-services registry,
    // otherwise it will trigger a delete flow until synced.
    @Column(name = "deleted_at", nullable = false)
    val deletedAt: Instant,
)
