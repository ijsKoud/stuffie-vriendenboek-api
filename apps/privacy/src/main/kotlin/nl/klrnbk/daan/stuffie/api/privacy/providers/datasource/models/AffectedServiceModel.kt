package nl.klrnbk.daan.stuffie.api.privacy.providers.datasource.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/** Which erasure a service has confirmed: one row per tombstone and service. */
@Entity
@Table(
    name = "affected_services",
    uniqueConstraints = [UniqueConstraint(columnNames = ["tombstone_id", "service_name"])],
)
data class AffectedServiceModel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    var id: String?,
    @Column(name = "tombstone_id", nullable = false)
    val tombstoneId: String,
    @Column(name = "service_name", nullable = false)
    val serviceName: String,
    // This date should be in sync with the tombstone registry,
    // otherwise it will trigger a delete flow until synced.
    @Column(name = "deleted_at", nullable = false)
    val deletedAt: Instant,
)
