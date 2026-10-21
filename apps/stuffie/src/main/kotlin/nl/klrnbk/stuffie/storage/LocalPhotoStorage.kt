package nl.klrnbk.stuffie.storage

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class LocalPhotoStorage(
    root: String,
) : PhotoStorage {
    private val root: Path = Path.of(root).toAbsolutePath().normalize()

    /** Resolves a key inside the root and refuses anything that escapes it. */
    private fun pathFor(key: String): Path {
        val resolved = root.resolve(key).normalize()
        require(resolved.startsWith(root)) { "Invalid storage key" }

        return resolved
    }

    override fun put(
        key: String,
        body: ByteArray,
    ) {
        val path = pathFor(key)
        path.parent.createDirectories()
        path.writeBytes(body)
    }

    override fun get(key: String): ByteArray? =
        try {
            pathFor(key).readBytes()
        } catch (_: NoSuchFileException) {
            null
        }

    override fun delete(key: String) {
        pathFor(key).deleteIfExists()
    }

    override fun deletePrefix(prefix: String) {
        require(prefix.isNotBlank()) { "Refusing to delete an empty prefix" }

        val path = pathFor(prefix)
        if (!path.exists()) return
        if (!path.isDirectory()) return path.deleteIfExists().let {}

        Files.walk(path).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { it.deleteIfExists() } }
    }
}
