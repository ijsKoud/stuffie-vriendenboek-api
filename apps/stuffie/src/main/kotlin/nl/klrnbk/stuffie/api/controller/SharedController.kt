package nl.klrnbk.stuffie.api.controller

import io.swagger.v3.oas.annotations.Operation
import nl.klrnbk.common.responses.error.InternalServerErrorResponse
import nl.klrnbk.common.responses.error.NotFoundResponse
import nl.klrnbk.stuffie.api.facade.SharedFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Public on purpose: the share token is the credential. No sign-in needed. */
@RestController
@RequestMapping("/v1/shared")
@NotFoundResponse
@InternalServerErrorResponse
class SharedController(
    private val sharedFacade: SharedFacade,
) {
    @GetMapping("/{token}")
    @Operation(summary = "A shared stuffie page, as seen by anyone with the link")
    fun getShared(
        @PathVariable token: String,
    ) = sharedFacade.getShared(token)

    @GetMapping("/{token}/photo", produces = ["image/jpeg"])
    @Operation(summary = "The photo of a shared stuffie")
    fun getSharedPhoto(
        @PathVariable token: String,
    ): ResponseEntity<ByteArray> = sharedFacade.getSharedPhoto(token)
}
