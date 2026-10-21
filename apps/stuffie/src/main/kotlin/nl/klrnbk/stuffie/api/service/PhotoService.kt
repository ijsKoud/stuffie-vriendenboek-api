package nl.klrnbk.stuffie.api.service

import nl.klrnbk.common.exceptions.BadRequestException
import nl.klrnbk.common.exceptions.NotFoundException
import nl.klrnbk.stuffie.datasource.models.PhotoModel
import nl.klrnbk.stuffie.datasource.repositories.PhotoRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieRepository
import nl.klrnbk.stuffie.datasource.repositories.StuffieShareRepository
import nl.klrnbk.stuffie.helpers.ImageProcessor
import nl.klrnbk.stuffie.helpers.SubHasher
import nl.klrnbk.stuffie.storage.PhotoStorage
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class PhotoService(
    private val photoRepository: PhotoRepository,
    private val stuffieRepository: StuffieRepository,
    private val shareRepository: StuffieShareRepository,
    private val photoStorage: PhotoStorage,
    private val imageProcessor: ImageProcessor,
    private val subHasher: SubHasher,
) {
    fun upload(
        ownerSub: String,
        content: ByteArray,
    ): PhotoModel {
        val jpeg = imageProcessor.toJpeg(content)

        val id = UUID.randomUUID().toString()
        val key = "${subHasher.hash(ownerSub)}/$id.jpg"
        photoStorage.put(key, jpeg)

        return try {
            photoRepository.save(PhotoModel(id, ownerSub, key, Instant.now()))
        } catch (exception: Exception) {
            photoStorage.delete(key)
            throw exception
        }
    }

    fun getOwnPhoto(
        ownerSub: String,
        photoId: String,
    ): ByteArray {
        val photo = photoRepository.findByIdAndOwnerSub(photoId, ownerSub).firstOrNull() ?: throw NotFoundException("Photo not found.")
        return photoStorage.get(photo.storageKey) ?: throw NotFoundException("Photo not found.")
    }

    /** The photo of the stuffie a share token exposes. */
    fun getSharedPhoto(token: String): ByteArray {
        val share = shareRepository.findByToken(token).firstOrNull() ?: throw NotFoundException("Photo not found.")
        val photoId = stuffieRepository.findById(share.stuffieId).orElse(null)?.photoId ?: throw NotFoundException("Photo not found.")
        val photo = photoRepository.findById(photoId).orElse(null) ?: throw NotFoundException("Photo not found.")

        return photoStorage.get(photo.storageKey) ?: throw NotFoundException("Photo not found.")
    }

    /** A stuffie may only use photos its owner uploaded. */
    fun assertOwned(
        ownerSub: String,
        photoId: String,
    ) {
        if (photoRepository.findByIdAndOwnerSub(photoId, ownerSub).isEmpty()) throw BadRequestException("Unknown photo id.")
    }

    fun delete(photoId: String) {
        val photo = photoRepository.findById(photoId).orElse(null) ?: return

        photoStorage.delete(photo.storageKey)
        photoRepository.delete(photo)
    }
}
