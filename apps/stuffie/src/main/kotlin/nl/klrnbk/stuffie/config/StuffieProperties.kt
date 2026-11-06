package nl.klrnbk.stuffie.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stuffie")
data class StuffieProperties(
    val privacy: PrivacyProperties,
    val storage: StorageProperties,
)

data class PrivacyProperties(
    /** Random, and never change it: it is baked into photo folder names and erasure tombstones. */
    val subHashSalt: String,
)

data class StorageProperties(
    /** `local` (disk, development) or `s3` (Garage on the NAS). */
    val driver: String,
    val localDir: String,
    val s3: S3Properties,
)

data class S3Properties(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
    val forcePathStyle: Boolean,
)
