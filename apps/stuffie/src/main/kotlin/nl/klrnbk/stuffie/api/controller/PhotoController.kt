package nl.klrnbk.stuffie.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nl.klrnbk.common.responses.error.BadRequestResponse
import nl.klrnbk.common.responses.error.BaseErrorResponses
import nl.klrnbk.common.responses.error.NotFoundResponse
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import nl.klrnbk.stuffie.api.facade.PhotoFacade
import nl.klrnbk.stuffie.api.models.PhotoUploadResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@SecurityRequirement(name = "api-key")
@RequestMapping("/v1/photos")
@BaseErrorResponses
class PhotoController(
    private val photoFacade: PhotoFacade,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload a photo",
        description = "Max 5 MB. Re-encoded as a JPEG of at most 1200px with all metadata removed. Attach it with the returned id.",
    )
    @BadRequestResponse
    fun upload(
        authentication: JwtAuthenticationToken,
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<PhotoUploadResponse> = photoFacade.upload(authentication.principal, file)

    @GetMapping("/{id}", produces = ["image/jpeg"])
    @Operation(summary = "Download one of your photos")
    @NotFoundResponse
    fun get(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
    ): ResponseEntity<ByteArray> = photoFacade.get(authentication.principal, id)
}
