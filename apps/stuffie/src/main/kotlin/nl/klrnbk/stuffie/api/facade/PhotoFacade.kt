package nl.klrnbk.stuffie.api.facade

import nl.klrnbk.common.exceptions.BadRequestException
import nl.klrnbk.stuffie.api.models.PhotoUploadResponse
import nl.klrnbk.stuffie.api.service.PhotoService
import org.slf4j.LoggerFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PhotoFacade(
    private val photoService: PhotoService,
) {
    private val logger = LoggerFactory.getLogger(PhotoFacade::class.java)

    fun upload(
        userId: String,
        file: MultipartFile,
    ): ResponseEntity<PhotoUploadResponse> {
        if (file.isEmpty) throw BadRequestException("The uploaded file is empty.")

        val photo = photoService.upload(userId, file.bytes)
        logger.info("Stored photo=${photo.id} for user=$userId")

        return ResponseEntity.status(HttpStatus.CREATED).body(PhotoUploadResponse(photo.id!!))
    }

    fun get(
        userId: String,
        photoId: String,
    ): ResponseEntity<ByteArray> =
        ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.noStore())
            .body(photoService.getOwnPhoto(userId, photoId))
}
