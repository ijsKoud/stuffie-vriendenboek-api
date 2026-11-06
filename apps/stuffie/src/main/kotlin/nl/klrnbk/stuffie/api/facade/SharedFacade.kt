package nl.klrnbk.stuffie.api.facade

import nl.klrnbk.stuffie.api.models.SharedStuffieDetails
import nl.klrnbk.stuffie.api.service.PhotoService
import nl.klrnbk.stuffie.api.service.StuffieService
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SharedFacade(
    private val stuffieService: StuffieService,
    private val photoService: PhotoService,
) {
    fun getShared(token: String): SharedStuffieDetails = stuffieService.getShared(token)

    fun getSharedPhoto(token: String): ResponseEntity<ByteArray> =
        ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
            .body(photoService.getSharedPhoto(token))
}
