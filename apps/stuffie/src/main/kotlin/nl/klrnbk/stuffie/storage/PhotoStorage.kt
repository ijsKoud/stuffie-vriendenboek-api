package nl.klrnbk.stuffie.storage

/** Everything the app needs from photo storage. Drivers: local disk (development) and S3 (Garage on the NAS). */
interface PhotoStorage {
    fun put(
        key: String,
        body: ByteArray,
    )

    fun get(key: String): ByteArray?

    fun delete(key: String)

    fun deletePrefix(prefix: String)
}
