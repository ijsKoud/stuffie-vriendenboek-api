package nl.klrnbk.daan.stuffie.api.privacy.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nl.klrnbk.common.responses.error.BaseErrorResponses
import nl.klrnbk.daan.stuffie.api.privacy.api.facade.DataDeleteFacade
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@BaseErrorResponses
@RestController
@RequestMapping("/v1/delete")
class DataDeleteController(
    private val dataDeleteFacade: DataDeleteFacade,
) {
    @DeleteMapping
    @Operation(
        summary = "Erase everything of the signed-in user (GDPR)",
        description =
            "Permanently removes all data, share links and photos and leaves one hashed tombstone. " +
                "The caller is responsible for the extra confirmation and fresh-login check, and for removing its own login records.",
    )
    @ApiResponse(
        responseCode = "204",
        description = "All data of the user has been erased",
        content = [Content(mediaType = "*/*", schema = Schema(implementation = Void::class))],
    )
    @ApiResponse(
        responseCode = "202",
        description = "The request is recorded, but not every service confirmed yet; it is retried automatically",
        content = [Content(mediaType = "*/*", schema = Schema(implementation = Void::class))],
    )
    fun deleteAllData(authentication: JwtAuthenticationToken) = dataDeleteFacade.requestDataDeletion(authentication)
}
